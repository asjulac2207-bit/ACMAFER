package com.acmafer.modulos.chatbot.servicio;

import com.acmafer.modulos.pedidos.repositorio.PedidoRepository;
import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import com.acmafer.modulos.tareas.entidad.Tarea;
import com.acmafer.modulos.tareas.repositorio.TareaRepository;
import com.acmafer.modulos.usuarios.entidad.Rol;
import com.acmafer.modulos.usuarios.repositorio.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ProductoRepository productoRepository;
    private final PedidoRepository   pedidoRepository;
    private final UsuarioRepository   usuarioRepository;
    private final TareaRepository     tareaRepository;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    // ═══════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ═══════════════════════════════════════════════════════════════
    public String responder(String mensaje, String rol) {
        String bloqueo = verificarPreguntaProhibida(mensaje, rol);
        if (bloqueo != null) return bloqueo;

        String contextoDb   = construirContextoDb(rol);
        String systemPrompt = construirSystemPrompt(rol, contextoDb);
        return llamarGroq(systemPrompt, mensaje);
    }

    // ═══════════════════════════════════════════════════════════════
    //  FILTRO DE PREGUNTAS PROHIBIDAS
    // ═══════════════════════════════════════════════════════════════
    private String verificarPreguntaProhibida(String mensaje, String rol) {
        String m = mensaje.toLowerCase();

        // Contraseñas — NADIE puede pedirlas
        if (m.matches(".*(contraseña|password|clave|passwd|pin|credencial).*") &&
            !m.contains("cambiar") && !m.contains("olvidé") && !m.contains("olvide") && !m.contains("recuperar")) {
            return "🔒 Por seguridad no puedo revelar contraseñas ni credenciales de ningún usuario. " +
                   "Si olvidaste la tuya puedes restablecerla en <a href='/perfil/cambiar-contrasena'>Cambiar contraseña</a>.";
        }

        // Datos personales de otros usuarios — solo ADMIN
        if (!rol.equals("ROLE_ADMINISTRADOR")) {
            boolean preguntaDatoPersonal =
                m.matches(".*(correo|email|celular|teléfono|telefono|documento|cédula|cedula).*(usuario|empleado|cliente|trabajador|vendedor|supervisor).*") ||
                m.matches(".*(usuario|empleado|cliente).*(correo|email|celular|teléfono|telefono|documento|cédula|cedula).*");
            if (preguntaDatoPersonal) {
                return "🔐 Los datos personales de otros usuarios solo los puede consultar el Administrador. " +
                       "¿Puedo ayudarte con algo más? 😊";
            }

            // Total de ventas en dinero — solo ADMIN
            boolean preguntaFinanciera =
                m.matches(".*(ventas totales|ingresos|facturación|facturacion|cuánto.*(vendido|facturado|generado)).*");
            if (preguntaFinanciera && !rol.equals("ROLE_SUPERVISOR")) {
                return "📊 Los datos financieros globales están disponibles solo para Administradores y Supervisores. " +
                       "¿Te ayudo con el catálogo o tus pedidos? 😊";
            }

            // Lista completa de usuarios — solo ADMIN
            boolean preguntaListaUsuarios =
                m.matches(".*(lista|todos los|cuántos|quiénes).*(usuarios|empleados|trabajadores|clientes).*");
            if (preguntaListaUsuarios) {
                return "👥 La gestión de usuarios es exclusiva del Administrador. ¿Puedo ayudarte con otra cosa? 😊";
            }
        }

        // Seguridad del sistema — nadie
        if (m.matches(".*(hackear|exploit|sql.?injection|bypass|token|jwt|acceso no autorizado|vulnerabilidad del sistema).*")) {
            return "🚫 Esa consulta no puedo atenderla. Si tienes una preocupación de seguridad real, reporta al administrador.";
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CONTEXTO DE BD FILTRADO POR ROL
    // ═══════════════════════════════════════════════════════════════
    private String construirContextoDb(String rol) {
        StringBuilder sb = new StringBuilder();
        boolean esAdmin     = rol.equals("ROLE_ADMINISTRADOR");
        boolean esSupervisor = rol.equals("ROLE_SUPERVISOR");
        boolean esAdminOSup  = esAdmin || esSupervisor;

        try {
            // ── Inventario ────────────────────────────────────────────────────
            long totalProductos  = productoRepository.count();
            long disponibles     = productoRepository.countByEstado("Disponible");
            long agotados        = productoRepository.countByEstado("Agotado");
            List<Producto> bajos = productoRepository.findConStockBajo();

            sb.append("=== INVENTARIO ===\n");
            sb.append("Total productos: ").append(totalProductos).append("\n");
            sb.append("Disponibles: ").append(disponibles).append("\n");
            sb.append("Agotados: ").append(agotados).append("\n");
            sb.append("Con stock bajo: ").append(bajos.size()).append("\n");

            if (esAdminOSup && !bajos.isEmpty()) {
                sb.append("Detalle stock crítico:\n");
                bajos.forEach(p -> sb.append("  - ").append(p.getNombre())
                        .append(" | Stock actual: ").append(p.getStockActual())
                        .append(" | Mínimo: ").append(p.getStockMinimo())
                        .append(" | Código: ").append(p.getCodigo() != null ? p.getCodigo() : "S/N").append("\n"));
            } else if (!bajos.isEmpty()) {
                sb.append("Productos con stock bajo (nombres): ");
                bajos.stream().limit(3).forEach(p -> sb.append(p.getNombre()).append(", "));
                sb.append("entre otros.\n");
            }

            // ── Top vendidos ──────────────────────────────────────────────────
            List<Producto> top = productoRepository.findTopVendidos(PageRequest.of(0, 5));
            if (!top.isEmpty()) {
                sb.append("\n=== TOP 5 MÁS VENDIDOS ===\n");
                for (int i = 0; i < top.size(); i++) {
                    Producto p = top.get(i);
                    sb.append(i + 1).append(". ").append(p.getNombre());
                    if (esAdminOSup) {
                        sb.append(" | Ventas totales: ").append(p.getVentasTotales())
                          .append(" | Stock: ").append(p.getStockActual())
                          .append(" | Precio: $").append(p.getPrecioUnitario() != null ? p.getPrecioUnitario() : "N/D");
                    }
                    sb.append("\n");
                }
            }

            // ── Catálogo disponible ───────────────────────────────────────────
            List<Producto> catalogo = productoRepository
                    .findByEstado("Disponible", PageRequest.of(0, 20)).getContent();
            if (!catalogo.isEmpty()) {
                sb.append("\n=== CATÁLOGO DISPONIBLE ===\n");
                catalogo.forEach(p -> {
                    sb.append("  • ").append(p.getNombre());
                    if (esAdminOSup) {
                        sb.append(" | Código: ").append(p.getCodigo() != null ? p.getCodigo() : "S/N")
                          .append(" | Stock: ").append(p.getStockActual())
                          .append(" | Precio: $").append(p.getPrecioUnitario() != null ? p.getPrecioUnitario() : "N/D")
                          .append(" | Estado: ").append(p.getEstado());
                    } else {
                        sb.append(" | Estado: ").append(p.getEstado());
                        if (p.getPrecioUnitario() != null) {
                            sb.append(" | Precio: $").append(p.getPrecioUnitario());
                        }
                    }
                    sb.append("\n");
                });
            }

            // ── Pedidos ───────────────────────────────────────────────────────
            sb.append("\n=== PEDIDOS ===\n");
            if (esAdminOSup) {
                sb.append("Pendientes: ").append(pedidoRepository.countByEstado("Pendiente")).append("\n");
                sb.append("En proceso: ").append(pedidoRepository.countByEstado("En proceso")).append("\n");
                sb.append("Entregados: ").append(pedidoRepository.countByEstado("Entregado")).append("\n");
                sb.append("Cancelados: ").append(pedidoRepository.countByEstado("Cancelado")).append("\n");
                if (esAdmin) {
                    var totalVentas = pedidoRepository.sumTotalVentas();
                    sb.append("Total ventas acumuladas: $").append(totalVentas != null ? totalVentas : 0).append("\n");
                }
            } else {
                sb.append("(El usuario puede consultar sus pedidos en /pedidos/mis-pedidos)\n");
            }

            // ── Usuarios — solo ADMIN con detalle completo ────────────────────
            if (esAdmin) {
                sb.append("\n=== USUARIOS ===\n");
                sb.append("Total: ").append(usuarioRepository.count()).append("\n");
                sb.append("Activos: ").append(usuarioRepository.countByEstado("Activo")).append("\n");
                sb.append("Inactivos: ").append(usuarioRepository.countByEstado("Inactivo")).append("\n");
                sb.append("Admins=").append(usuarioRepository.countByRol(Rol.ADMINISTRADOR))
                  .append(", Supervisores=").append(usuarioRepository.countByRol(Rol.SUPERVISOR))
                  .append(", Trabajadores=").append(usuarioRepository.countByRol(Rol.TRABAJADOR))
                  .append(", Vendedores=").append(usuarioRepository.countByRol(Rol.VENDEDOR))
                  .append(", Clientes=").append(usuarioRepository.countByRol(Rol.CLIENTE)).append("\n");
            }

            // ── Tareas — Admin y Supervisor con detalle ───────────────────────
            if (esAdminOSup) {
                sb.append("\n=== TAREAS ===\n");
                sb.append("Sin iniciar: ").append(tareaRepository.countByEstado(Tarea.Estado.NO_INICIADA)).append("\n");
                sb.append("En progreso: ").append(tareaRepository.countByEstado(Tarea.Estado.EN_PROGRESO)).append("\n");
                sb.append("Bloqueadas: ").append(tareaRepository.countByEstado(Tarea.Estado.BLOQUEADA)).append("\n");
                sb.append("Completadas: ").append(tareaRepository.countByEstado(Tarea.Estado.COMPLETADA)).append("\n");
            }

        } catch (Exception e) {
            log.warn("Error leyendo contexto DB para chatbot: {}", e.getMessage());
            sb.append("(Error temporal al consultar la base de datos)\n");
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════
    //  SYSTEM PROMPT
    // ═══════════════════════════════════════════════════════════════
    private String construirSystemPrompt(String rol, String contextoDb) {

        String reglasPrivacidad = """
            REGLAS DE PRIVACIDAD — CUMPLIMIENTO OBLIGATORIO:
            1. NUNCA reveles contraseñas, hashes, tokens ni credenciales de ningún usuario.
            2. NUNCA menciones datos personales (emails, teléfonos, documentos) de usuarios específicos \
            a menos que el rol sea ADMINISTRADOR.
            3. NUNCA deduzcas información que no esté explícitamente en el contexto de base de datos.
            4. NUNCA proporciones información de seguridad interna (endpoints, estructura de BD, tokens JWT).
            5. Si el usuario pide algo fuera de su alcance, responde amablemente que está restringido.
            6. El total de dinero en ventas SOLO lo compartes con ADMINISTRADOR.
            7. Precios y stock de productos individuales puedes compartirlos con TODOS los roles.
            """;

        String empresa = """
            SOBRE ACMAFER:
            ACMAFER es una empresa colombiana de fundición y manufactura industrial de metales, en Sogamoso, Boyacá. \
            Fabricamos piezas en hierro gris, hierro nodular, acero y bronce para los sectores minero, agroindustrial \
            y de construcción. Somos reconocidos por calidad, precisión y responsabilidad industrial.
            """;

        String permisosYNav = switch (rol) {
            case "ROLE_ADMINISTRADOR" -> """
                ROL: ADMINISTRADOR — acceso total.
                Puedes responder con TODOS los datos del sistema sin restricción.
                Navegación: /dashboard, /productos/catalogo, /productos/nuevo, /pedidos/todos, /usuarios,
                /tareas/admin, /reportes, /rendimiento, /perfil.
                """;
            case "ROLE_SUPERVISOR" -> """
                ROL: SUPERVISOR — acceso amplio excepto datos financieros globales y usuarios.
                Navegación: /dashboard, /productos/catalogo, /pedidos/todos, /tareas/admin, /reportes,
                /rendimiento, /perfil.
                NO muestres el total de ventas en dinero ni datos personales de otros usuarios.
                """;
            case "ROLE_TRABAJADOR" -> """
                ROL: TRABAJADOR
                Navegación: /dashboard, /productos/catalogo, /pedidos/mis-pedidos, /tareas/mis-tareas,
                /rendimiento/mi-rendimiento, /perfil.
                Solo información general de productos y sus propios pedidos/tareas.
                """;
            case "ROLE_VENDEDOR" -> """
                ROL: VENDEDOR
                Navegación: /dashboard, /productos/catalogo, /pedidos/crear, /pedidos/mis-pedidos, /pedidos/todos, /perfil.
                Puedes informar sobre productos, precios y cómo hacer pedidos.
                """;
            case "ROLE_CLIENTE" -> """
                ROL: CLIENTE
                Navegación: /productos/catalogo, /pedidos/mis-pedidos, /perfil.
                Enfócate en ayudar a encontrar productos y hacer pedidos. \
                No compartas estadísticas internas ni datos de otros usuarios.
                """;
            default -> """
                ROL: VISITANTE PÚBLICO (no autenticado)
                Solo habla sobre la empresa, sus servicios y cómo registrarse.
                Navegación: /, /auth/login, /auth/registro.
                NO compartas ningún dato interno del sistema.
                """;
        };

        return String.format("""
            Eres AcmaBot 🤖, el asistente virtual oficial de ACMAFER.
            
            PERSONALIDAD:
            - Eres MUY alegre, entusiasta, paciente y servicial 🎉😊
            - Usas emojis con moderación para una conversación amigable
            - Si no entiendes algo, preguntas con amabilidad
            - Guías al usuario paso a paso sin abrumarlo
            - Cuando menciones una sección, incluye el enlace: <a href='/ruta'>Nombre</a>
            - Respuestas claras y concretas, máximo 4 párrafos
            - Al final pregunta si hay algo más en que puedas ayudar
            - Si el usuario busca un producto, recomienda opciones del catálogo real con nombre y precio
            - NUNCA inventes datos: usa SOLO los del contexto de base de datos
            
            %s
            
            %s
            
            %s
            
            DATOS REALES DEL SISTEMA AHORA MISMO:
            %s
            
            Responde siempre en español.
            """, reglasPrivacidad, empresa, permisosYNav, contextoDb);
    }

    // ═══════════════════════════════════════════════════════════════
    //  LLAMADA A GROQ API
    // ═══════════════════════════════════════════════════════════════
    private String llamarGroq(String systemPrompt, String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "openai/gpt-oss-20b");
            body.put("max_tokens", 600);
            body.put("temperature", 0.65);
            body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user",   "content", userMessage)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(groqApiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<?> choices = (List<?>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> first = (Map<?, ?>) choices.get(0);
                    Map<?, ?> msg   = (Map<?, ?>) first.get("message");
                    if (msg != null) return (String) msg.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error llamando Groq API: {}", e.getMessage());
        }
        return "¡Ups! 😅 Tuve un problemita de conexión. Puedes explorar el " +
               "<a href='/productos/catalogo'>Catálogo</a> o revisar " +
               "<a href='/pedidos/mis-pedidos'>Mis Pedidos</a>. ¿Necesitas algo más?";
    }
}
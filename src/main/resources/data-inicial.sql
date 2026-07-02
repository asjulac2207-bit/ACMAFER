-- ═══════════════════════════════════════════════════════════════════
-- ACMAFER — Script de datos iniciales
-- Base de datos: ProyectoACMAFER (SQL Server)
-- Las contraseñas son BCrypt strength=12 de "acmafer2026"
-- ═══════════════════════════════════════════════════════════════════

USE ProyectoACMAFER;
GO

-- Limpiar orden correcto (FK)
DELETE FROM asignacionTarea;
DELETE FROM comentarioTarea;
DELETE FROM detallePedido;
DELETE FROM historialPedido;
DELETE FROM historialProducto;
DELETE FROM tokenRecuperacion;
DELETE FROM notificacion;
DELETE FROM pedido;
DELETE FROM producto;
DELETE FROM categoria;
DELETE FROM usuario;
GO

-- ─── Usuarios ────────────────────────────────────────────────────────
-- Contraseña de todos: acmafer2026 (hash BCrypt)
INSERT INTO usuario (documento, nombre, apellido, email, celular, clave, idRol, estado, intentosFallidos, bloqueado, fechaRegistro)
VALUES
-- Administrador
('1000000001', 'Carlos',   'Mendoza',   'admin@acmafer.com',      '3001234567',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8PiN8JQB/JQWK7uFWWG', 'ADMINISTRADOR', 'Activo', 0, 0, GETDATE()),
-- Supervisor
('1000000002', 'Marcela',  'Ríos',      'supervisor@acmafer.com', '3112345678',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8PiN8JQB/JQWK7uFWWG', 'SUPERVISOR',    'Activo', 0, 0, GETDATE()),
-- Trabajador
('1000000003', 'Andrés',   'Castillo',  'trabajador@acmafer.com', '3223456789',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8PiN8JQB/JQWK7uFWWG', 'TRABAJADOR',    'Activo', 0, 0, GETDATE()),
-- Vendedor (nuevo — RF-04)
('1000000004', 'Daniela',  'Torres',    'vendedor@acmafer.com',   '3334567890',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8PiN8JQB/JQWK7uFWWG', 'VENDEDOR',      'Activo', 0, 0, GETDATE()),
-- Cliente
('1000000005', 'Juan',     'Pérez',     'cliente@acmafer.com',    '3445678901',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8PiN8JQB/JQWK7uFWWG', 'CLIENTE',       'Activo', 0, 0, GETDATE());
GO

-- ─── Categorías ──────────────────────────────────────────────────────
INSERT INTO categoria (nombre, descripcion, activo) VALUES
('Fundición',        'Piezas y componentes de fundición industrial',             1),
('Maquinaria',       'Partes y repuestos para maquinaria industrial',             1),
('Materia Prima',    'Materiales base para procesos de fundición y manufactura', 1),
('Herramientas',     'Herramientas manuales y de precisión industrial',           1),
('Equipos de Seguridad', 'Elementos de protección personal industrial',          1);
GO

-- ─── Productos ───────────────────────────────────────────────────────
INSERT INTO producto (nombre, descripcion, codigo, stockActual, stockMinimo, estado, precioUnitario, idCategoria, fechaCreacion)
VALUES
('Lingote de hierro gris',   'Hierro gris de alta pureza para fundición',        'ACM-001', 150, 20, 'Disponible', 85000.00,  1, GETDATE()),
('Lingote de acero carbono', 'Acero carbono AISI 1020 para manufactura',         'ACM-002', 80,  10, 'Disponible', 120000.00, 1, GETDATE()),
('Pieza fundida ref. A-200', 'Pieza de fundición gris para transmisiones',       'ACM-003', 35,  5,  'Disponible', 450000.00, 1, GETDATE()),
('Pieza fundida ref. B-350', 'Componente de fundición nodular tratada',          'ACM-004', 12,  5,  'Disponible', 680000.00, 1, GETDATE()),
('Rodamiento SKF 6205',      'Rodamiento de bolas de alta carga',                'ACM-010', 200, 30, 'Disponible', 45000.00,  2, GETDATE()),
('Engranaje cónico M-4',     'Engranaje de acero templado módulo 4',             'ACM-011', 25,  5,  'Disponible', 390000.00, 2, GETDATE()),
('Arrabio grado 1',          'Arrabio Foundry Grade para alta temperatura',      'ACM-020', 500, 50, 'Disponible', 62000.00,  3, GETDATE()),
('Chatarra de acero',        'Chatarra limpia clasificada A1',                   'ACM-021', 1000,100,'Disponible', 18000.00,  3, GETDATE()),
('Coquilla aluminio 6061',   'Aleación de aluminio T6 para fundición en coquilla','ACM-022',60,  10, 'Disponible', 155000.00, 3, GETDATE()),
('Fresa de punta plana HSS', 'Fresa de alta velocidad Ø12mm x 4 filos',         'ACM-030', 40,  8,  'Disponible', 78000.00,  4, GETDATE()),
('Calibrador digital 150mm', 'Calibrador vernier digital IP54',                  'ACM-031', 15,  3,  'Disponible', 125000.00, 4, GETDATE()),
('Casco industrial clase A', 'Casco de seguridad ANSI Z89.1',                   'ACM-040', 80,  15, 'Disponible', 38000.00,  5, GETDATE()),
('Guantes refractarios',     'Guantes kevlar para altas temperaturas (hasta 250°C)', 'ACM-041', 3, 5, 'Disponible', 95000.00, 5, GETDATE());
GO

PRINT '✅ Datos iniciales insertados correctamente — ACMAFER';
GO

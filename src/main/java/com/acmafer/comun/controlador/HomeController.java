package com.acmafer.comun.controlador;

import com.acmafer.modulos.productos.entidad.Producto;
import com.acmafer.modulos.productos.repositorio.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
        if (loggedIn)
            return "redirect:/dashboard";

        List<Producto> masVendidos = productoRepository
                .findTopVendidos(PageRequest.of(0, 6)); // ← findTopVendidos, no findTop6MasVendidos
        model.addAttribute("masVendidos", masVendidos);

        model.addAttribute("titulo", "Bienvenido a ACMAFER");
        model.addAttribute("mostrarIntro", true);
        return "index";
    }
}
package com.acmafer.comun.seguridad;

import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.UsuarioDetailsService;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final UsuarioService usuarioService;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Value("${acmafer.bcrypt.strength:10}")
    private int bcryptStrength;

    public SecurityConfig(UsuarioDetailsService usuarioDetailsService,
            @Lazy UsuarioService usuarioService,
            CustomOAuth2UserService customOAuth2UserService) {
        this.usuarioDetailsService = usuarioDetailsService;
        this.usuarioService = usuarioService;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider ap = new DaoAuthenticationProvider();
        ap.setUserDetailsService(usuarioDetailsService);
        ap.setPasswordEncoder(passwordEncoder());
        return ap;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                "/h2-console/**",
                "/api/**",
                "/chatbot/**",
                "/pedidos/pago/confirmacion",
                "/pedidos/checkout/preparar-pago-epayco"))

                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // ── Recursos públicos ──────────────────────────────────
                        .requestMatchers(
                                "/", "/auth/**", "/css/**", "/js/**", "/img/**",
                                "/uploads/**", "/h2-console/**", "/error",
                                "/pedidos/pago/confirmacion",
                                "/pedidos/pago/respuesta")
                        .permitAll()

                        // ── Solo ADMINISTRADOR ─────────────────────────────────
                        .requestMatchers("/admin/**", "/usuarios/**")
                        .hasRole("ADMINISTRADOR")

                        // ── VENDEDOR — Rutas específicas de ventas ─────────────
                        .requestMatchers("/reportes/ventas", "/reportes/pdf/ventas-vendedor")
                        .hasRole("VENDEDOR")

                        // ── ADMINISTRADOR y SUPERVISOR ─────────────────────────
                        .requestMatchers("/reportes/**")
                        .hasAnyRole("ADMINISTRADOR", "SUPERVISOR")

                        // ── ADMINISTRADOR, SUPERVISOR y VENDEDOR ───────────────
                        .requestMatchers("/pedidos/todos/**")
                        .hasAnyRole("ADMINISTRADOR", "SUPERVISOR", "VENDEDOR")

                        // ── Cualquier autenticado ──────────────────────────────
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(successHandler())
                        .failureHandler(failureHandler())
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(failureHandler()))

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .sessionManagement(s -> s.maximumSessions(5));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (req, res, auth) -> {
            res.sendRedirect("/dashboard");
        };
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
                Usuario usuario = customOAuth2User.getUsuario();

                UsernamePasswordAuthenticationToken nuevaAuth =
                        new UsernamePasswordAuthenticationToken(
                                usuario, null, usuario.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(nuevaAuth);

                HttpSession session = request.getSession(true);
                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());

                successHandler().onAuthenticationSuccess(request, response, nuevaAuth);
            } else {
                successHandler().onAuthenticationSuccess(request, response, authentication);
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (req, res, ex) -> {
            String tipo;
            if (ex instanceof org.springframework.security.authentication.DisabledException) {
                tipo = "inactiva";
            } else if (ex instanceof org.springframework.security.authentication.LockedException) {
                tipo = "bloqueada";
            } else {
                tipo = "credenciales";
            }
            res.sendRedirect("/auth/login?error=" + tipo);
        };
    }
}
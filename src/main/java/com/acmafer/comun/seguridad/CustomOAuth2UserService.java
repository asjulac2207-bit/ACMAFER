package com.acmafer.comun.seguridad;

import com.acmafer.modulos.usuarios.entidad.Usuario;
import com.acmafer.modulos.usuarios.servicio.UsuarioService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsuarioService usuarioService;

    public CustomOAuth2UserService(@Lazy UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String nombreCompleto = oauth2User.getAttribute("name");
        String fotoUrl = oauth2User.getAttribute("picture");

        Usuario usuario = usuarioService.buscarOCrearPorGoogle(email, nombreCompleto, fotoUrl);

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        return new CustomOAuth2User(usuario, oauth2User.getAttributes(), authorities);
    }
}
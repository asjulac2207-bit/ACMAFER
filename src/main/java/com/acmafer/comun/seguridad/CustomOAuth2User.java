package com.acmafer.comun.seguridad;

import com.acmafer.modulos.usuarios.entidad.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final Usuario usuario;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(Usuario usuario, Map<String, Object> attributes,
            Collection<? extends GrantedAuthority> authorities) {
        this.usuario = usuario;
        this.attributes = attributes;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return usuario.getEmail();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // Métodos que tu plantilla espera, delegados al Usuario real
    public Long getId() {
        return usuario.getId();
    }

    public String getNombre() {
        return usuario.getNombre();
    }

    public String getApellido() {
        return usuario.getApellido();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }

    public String getIniciales() {
        return usuario.getIniciales();
    }

    public Object getRol() {
        return usuario.getRol();
    }
}
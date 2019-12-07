package com.groudina.ten.demo.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

@Getter
public class JwtResponse implements Serializable {
    private String accessToken;
    private String email;
    private Collection<GrantedAuthority> authorities;
    public final String type = "Bearer";

    public JwtResponse(String accessToken, String email, Collection<GrantedAuthority> authorities) {
        this.accessToken = accessToken;
        this.email = email;
        this.authorities = authorities;
    }

    private static final long serialVersionUID = -1764970284520382345L;
}

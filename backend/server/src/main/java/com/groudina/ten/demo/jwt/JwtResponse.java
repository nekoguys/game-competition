package com.groudina.ten.demo.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

@Data
public class JwtResponse implements Serializable {
    @JsonProperty
    private String accessToken;

    @JsonProperty
    private String email;

    @JsonProperty
    private Collection<GrantedAuthority> authorities;

    @JsonProperty
    private long expirationTimestamp;

    public final String type = "Bearer";

    public JwtResponse(String accessToken, String email, Collection<GrantedAuthority> authorities, long expirationTimestamp) {
        this.accessToken = accessToken;
        this.email = email;
        this.authorities = authorities;
        this.expirationTimestamp = expirationTimestamp;
    }

    private static final long serialVersionUID = -1764970284520382345L;
}

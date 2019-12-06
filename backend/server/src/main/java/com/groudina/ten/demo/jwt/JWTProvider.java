package com.groudina.ten.demo.jwt;

import com.groudina.ten.demo.datasource.DbUserRepository;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
public class JWTProvider {
    private final Logger logger = LoggerFactory.getLogger(JWTProvider.class);

    @Value("${groudina.app.jwtSecret}")
    private String jwtSecret;

    @Value("${groudina.app.jwtExpiration}")
    private Integer jwtExpiration = 0;

    public String generateJwtToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + jwtExpiration * 1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            logger.error(String.format("Invalid JwtSignature: error: %s", e.getMessage()));
        } catch (MalformedJwtException e) {
            logger.error(String.format("Invalid JwtToken: error: %s", e.getMessage()));
        } catch (ExpiredJwtException e) {
            logger.error(String.format("Expired JWT token: error: %s", e.getMessage()));
        } catch (UnsupportedJwtException e) {
            logger.error(String.format("Unsupported JWT token: error: %s", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error(String.format("JWT claims string is empty: error: %s", e.getMessage()));
        }

        return false;
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }
}

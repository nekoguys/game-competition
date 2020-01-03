package com.groudina.ten.demo.jwt;

import com.groudina.ten.demo.datasource.DbUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private ReactiveUserDetailsService userDetailsService;

    private JWTProvider jwtProvider;

    public AuthenticationManager(@Autowired JWTProvider provider, @Autowired UserDetailsServiceImpl userDetailsService) {
        this.jwtProvider = provider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        if (jwtProvider.validateJwtToken(authToken)) {
            String email = jwtProvider.getEmailFromJwtToken(authToken);
            return userDetailsService.findByUsername(email).map(userDetails -> {

                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                return auth;
            });
        } else {
            return Mono.empty();
        }
    }
}

package com.groudina.ten.demo.jwt;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {
    private static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
        String authHeader = serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            String authToken = authHeader.replace(TOKEN_PREFIX, "");
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);

            return authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
        } else {
            log.warn("There is no 'Bearer ' auth header");
            return Mono.empty();
        }
    }
}

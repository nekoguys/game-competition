package ru.nekoguys.game.web.security

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class SecurityContextRepository (
    private val authenticationManager: GameAuthenticationManager,
) : ServerSecurityContextRepository {

    override fun save(serverWebExchange: ServerWebExchange, securityContext: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(serverWebExchange: ServerWebExchange): Mono<SecurityContext> {
        val authHeader: String? =
            serverWebExchange
                .request
                .headers
                .getFirst(HttpHeaders.AUTHORIZATION)

        return if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            val authToken = authHeader.replace(TOKEN_PREFIX, "")
            createContextFromToken(authToken)
        } else {
            logger.warn("There is no 'Bearer ' auth header")
            Mono.empty()
        }
    }

    private fun createContextFromToken(authToken: String): Mono<SecurityContext> {
        val auth = UsernamePasswordAuthenticationToken(authToken, authToken)
        return authenticationManager
            .authenticate(auth)
            .map(::SecurityContextImpl)
    }

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
        private val logger = LoggerFactory.getLogger(SecurityContextRepository::class.java)
    }
}

package ru.nekoguys.game.web.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.security.GameUserDetailsService
import ru.nekoguys.game.web.util.REQUEST_ID_CONTEXT_KEY
import ru.nekoguys.game.web.util.extractRequestId

@Component
class JwtAuthenticationConverter(
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
    private val userDetailsService: GameUserDetailsService,
) : ServerAuthenticationConverter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return convertInternal(exchange)
            .doOnEach { signal ->
                val authentication = signal.get()
                if (authentication != null && logger.isInfoEnabled) {
                    val requestId = signal.contextView.extractRequestId()
                    MDC.putCloseable(REQUEST_ID_CONTEXT_KEY, requestId).use {
                        val username = authentication.principal
                        val authorities = authentication.authorities.joinToString { it.toString() }
                        logger.info("Authenticated as $username with authorities $authorities")
                    }
                }
            }
    }

    private fun convertInternal(exchange: ServerWebExchange): Mono<Authentication> {
        val serializedJwt: String =
            exchange
                .extractAuthHeader()
                ?.removePrefix(TOKEN_PREFIX)
                ?: return Mono.empty()

        val jwt: Jws<Claims> =
            jwtProvider
                .parseJwt(serializedJwt)
                ?: return Mono.empty()

        return if (jwtProperties.fastAuthenticationEnabled) {
            authenticateByJwtClaims(jwt.body)
        } else {
            authenticateByEmail(jwt.body.subject)
        }
    }

    private fun ServerWebExchange.extractAuthHeader(): String? =
        request
            .headers
            .getFirst(HttpHeaders.AUTHORIZATION)

    private fun authenticateByJwtClaims(claims: Claims): Mono<Authentication> {
        val authorities = claims.get("roles", String::class.java)
            .split(';')
            .map { SimpleGrantedAuthority("ROLE_$it") }

        val authentication =
            UsernamePasswordAuthenticationToken(claims.subject, null, authorities)

        return Mono.just(authentication)
    }

    private fun authenticateByEmail(email: String): Mono<Authentication> =
        userDetailsService
            .findByUsername(email)
            .map(UserDetails::toAuthentication)

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
    }
}

private fun UserDetails.toAuthentication(): Authentication =
    UsernamePasswordAuthenticationToken(this, null, this.authorities)

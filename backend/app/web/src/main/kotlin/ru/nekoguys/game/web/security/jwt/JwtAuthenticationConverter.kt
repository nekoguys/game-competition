package ru.nekoguys.game.web.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
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
import ru.nekoguys.game.web.util.withMDCContext

@Component
class JwtAuthenticationConverter(
    private val jwtProperties: JwtProperties,
    private val jwtProvider: JwtProvider,
    private val userDetailsService: GameUserDetailsService,
) : ServerAuthenticationConverter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> = mono {
        withMDCContext {
            convertInternal(exchange)
                ?.also { authentication ->
                    val username = authentication.principal
                    val authorities = authentication.authorities.joinToString()
                    logger.info("Authenticated as $username with authorities $authorities")
                }
        }
    }

    private suspend fun convertInternal(
        exchange: ServerWebExchange
    ): Authentication? {
        val serializedJwt: String =
            exchange
                .extractAuthHeader()
                ?.removePrefix(TOKEN_PREFIX)
                ?: return null

        val jwt: Jws<Claims> =
            jwtProvider
                .parseJwt(serializedJwt)
                ?: return null

        return if (jwtProperties.fastAuthenticationEnabled) {
            authenticateByJwtClaims(jwt.body)
        } else {
            authenticateByEmail(jwt.body.subject)
        }
    }

    private fun authenticateByJwtClaims(claims: Claims): Authentication {
        val authorities = claims.get("roles", String::class.java)
            .split(';')
            .map { SimpleGrantedAuthority("ROLE_$it") }

        return UsernamePasswordAuthenticationToken(
            claims.subject,
            null,
            authorities,
        )
    }

    private suspend fun authenticateByEmail(email: String): Authentication? =
        userDetailsService
            .findByUsernameSuspending(email)
            ?.let(UserDetails::toAuthentication)

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
    }
}

private fun ServerWebExchange.extractAuthHeader(): String? =
    request
        .headers
        .getFirst(HttpHeaders.AUTHORIZATION)

private fun UserDetails.toAuthentication(): Authentication =
    UsernamePasswordAuthenticationToken(this, null, this.authorities)

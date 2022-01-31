package ru.nekoguys.game.web.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.security.GameUserDetailsService

@Component
class JwtAuthenticationConverter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: GameUserDetailsService,
) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val serializedJwt: String =
            exchange
                .extractAuthHeader()
                ?.removePrefix(TOKEN_PREFIX)
                ?: return Mono.empty()

        val jwt: Jws<Claims> =
            jwtProvider
                .parseJwt(serializedJwt)
                ?: return Mono.empty()

        return authenticateByEmail(jwt.body.subject)
    }

    private fun ServerWebExchange.extractAuthHeader(): String? =
        request
            .headers
            .getFirst(HttpHeaders.AUTHORIZATION)

    private fun authenticateByEmail(email: String): Mono<Authentication> =
        userDetailsService
            .findByUsername(email)
            .map(UserDetails::toAuthentication)
//            .flatMap(Authentication::saveInContextHolder)

    companion object {
        private const val TOKEN_PREFIX = "Bearer "
    }
}

private fun UserDetails.toAuthentication(): Authentication =
    UsernamePasswordAuthenticationToken(this, null, this.authorities)

private fun Authentication.saveInContextHolder(): Mono<Authentication> =
    ReactiveSecurityContextHolder
        .getContext()
        .map { it.authentication = this }
        .then(Mono.just(this))

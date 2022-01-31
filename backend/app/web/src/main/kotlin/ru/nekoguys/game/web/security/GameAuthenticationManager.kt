package ru.nekoguys.game.web.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.security.jwt.JwtProvider

class GameAuthenticationManager(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: GameUserDetailsService,
) {

    fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()
        val jwt: Jws<Claims> =
            jwtProvider.parseJwt(authToken)
                ?: return Mono.empty()

        return authenticateByEmail(jwt.body.subject)
    }

    private fun authenticateByEmail(email: String): Mono<Authentication> =
        userDetailsService
            .findByUsername(email)
            .then(Mono.empty())
}


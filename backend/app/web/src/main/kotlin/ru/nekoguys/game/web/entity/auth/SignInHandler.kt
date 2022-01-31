package ru.nekoguys.game.web.entity.auth

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import ru.nekoguys.game.web.security.jwt.JwtProvider

@Service
class SignInHandler(
    private val jwtProvider: JwtProvider,
    private val authenticationManager: ReactiveAuthenticationManager,
) {
    suspend fun signIn(request: SignInRequest): ResponseEntity<SignInResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        ).awaitFirstOrNull() ?: return ResponseEntity.badRequest().build()

        val userDetails = authentication.principal as UserDetails
        val jwt = jwtProvider.generateJwtToken(authentication)

        val response = SignInResponse(
            bearerToken = jwt,
            email = userDetails.username,
            authorities = userDetails.authorities,
        )
        return ResponseEntity.ok(response)
    }
}

data class SignInRequest(
    val username: String,
    val password: String,
)

data class SignInResponse(
    val bearerToken: String,
    val email: String,
    val authorities: Collection<GrantedAuthority>,
)

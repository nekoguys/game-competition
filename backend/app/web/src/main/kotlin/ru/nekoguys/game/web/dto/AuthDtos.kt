package ru.nekoguys.game.web.dto

import org.springframework.security.core.GrantedAuthority

data class SignInRequest(
    val email: String,
    val password: String,
)

sealed interface SignInResponse {
    data class Success(
        val accessToken: String,
        val email: String,
        val authorities: Collection<GrantedAuthority>,
        val expirationTimestamp: Long,
    ) : SignInResponse

    object InvalidCredentials : SignInResponse {
        val message = "Invalid credentials"
    }
}

data class SignUpRequest(
    val email: String,
    val password: String,
)

sealed interface SignUpResponse {
    object Success : SignUpResponse {
        val message = "User registered successfully!"
    }

    data class Error(
        val message: String,
    ) : SignUpResponse
}


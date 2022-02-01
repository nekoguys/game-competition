package ru.nekoguys.game.web.dto

import org.springframework.security.core.GrantedAuthority

data class SignInRequest(
    val email: String,
    val password: String,
)

data class SignInResponse(
    val accessToken: String,
    val email: String,
    val authorities: Collection<GrantedAuthority>,
    val expirationTimestamp: Long,
)

data class SignUpRequest(
    val email: String,
    val password: String,
)

sealed interface SignUpResponse {
    data class Success(
        val id: Long,
    ) : SignUpResponse

    data class Error(
        val message: String,
    ) : SignUpResponse
}


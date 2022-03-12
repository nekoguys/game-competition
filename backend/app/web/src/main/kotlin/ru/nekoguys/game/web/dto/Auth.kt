@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

data class SignInRequest(
    val email: String,
    val password: String,
)

data class SignInResponse(
    val accessToken: String,
    val email: String,
    val authorities: Collection<Authority>,
    val expirationTimestamp: Long,
) : WebResponse(HttpStatus.OK)

data class Authority(
    val authority: String,
)

data class SignUpRequest(
    val email: String,
    val password: String,
)

sealed interface SignUpResponse {
    object Success : SignUpResponse {
        @Suppress("unused")
        val message = "User registered successfully!"
    }

    class InvalidEmailFormat(email: String) : SignUpResponse {
        @Suppress("unused")
        val message = listOf(
            "Invalid email $email",
            "Email should end with @edu.hse.ru or @hse.ru"
        ).joinToString(" ")
    }

    class UserAlreadyRegistered(email: String) : SignUpResponse {
        @Suppress("unused")
        val message = "User with email $email already exists!"
    }
}


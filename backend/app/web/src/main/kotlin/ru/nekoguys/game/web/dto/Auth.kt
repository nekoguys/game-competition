package ru.nekoguys.game.web.dto

data class SignInRequest(
    val email: String,
    val password: String,
)

data class Authority(
    val authority: String,
)

data class SignInResponse(
    val accessToken: String,
    val email: String,
    val authorities: Collection<Authority>,
    val expirationTimestamp: Long,
)

data class SignUpRequest(
    val email: String,
    val password: String,
)

sealed interface SignUpResponse {
    object Success : SignUpResponse {
        @Suppress("MayBeConstant")
        val message = "User registered successfully!"
    }

    class InvalidEmailFormat(email: String) : SignUpResponse {
        val message = listOf(
            "Invalid email $email",
            "Email should end with @edu.hse.ru or @hse.ru"
        ).joinToString(" ")
    }

    class UserAlreadyRegistered(email: String) : SignUpResponse {
        val message = "User with email $email already exists!"
    }
}


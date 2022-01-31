package ru.nekoguys.game.web.entity.auth

import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository

@Component
class SignUpHandler(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun signUp(request: SignUpRequest): ResponseEntity<SignUpResponse> {
        val email = request.username.lowercase().trim()

        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            return ResponseEntity.badRequest().build()
        }

        val createdUser = userRepository.create(
            email = email,
            password = passwordEncoder.encode(request.password),
            role = when {
                email.endsWith("@edu.hse.ru") -> UserRole.Student
                email.endsWith("@admin.hse.ru") -> UserRole.Admin
                email.endsWith("@hse.ru") -> UserRole.Teacher
                else -> return ResponseEntity
                    .badRequest()
                    .body("email $email has unknown suffix".toErrorMessage())
            },
        )

        val response = SignUpResponse.Success(
            id = createdUser.id.number,
        )
        return ResponseEntity.ok(response)
    }
}

data class SignUpRequest(
    val username: String,
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

fun String.toErrorMessage() = SignUpResponse.Error(this)

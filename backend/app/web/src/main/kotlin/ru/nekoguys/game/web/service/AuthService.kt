package ru.nekoguys.game.web.service

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.*
import ru.nekoguys.game.web.security.jwt.JwtProvider

@Service
class AuthService(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
) {

    suspend fun signIn(request: SignInRequest): SignInResponse {
        val token =
            UsernamePasswordAuthenticationToken(request.email, request.password)

        val authentication = authenticationManager
            .authenticate(token)
            .awaitFirst()

        return authentication.toSignInResponse()
    }

    private fun Authentication.toSignInResponse(): SignInResponse {
        val expirationTimestamp = jwtProvider.generateExpirationTimestamp()
        val jwt = jwtProvider.generateJwtToken(
            authentication = this,
            expirationTimestamp,
        )

        val userDetails = principal as UserDetails
        return SignInResponse(
            accessToken = jwt,
            email = userDetails.username,
            authorities = userDetails
                .authorities
                .map { it.toString() }
                .map { Authority(it) },
            expirationTimestamp = expirationTimestamp,
        )
    }

    suspend fun signUp(request: SignUpRequest): SignUpResponse {
        val email = request.email
            .lowercase()
            .trim()

        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            return SignUpResponse.UserAlreadyRegistered(email)
        }

        userRepository.create(
            email = email,
            password = passwordEncoder.encode(request.password),
            role = when {
                email.endsWith("@edu.hse.ru") -> UserRole.Student
                email.endsWith("@hse.ru") -> UserRole.Teacher
                else -> return SignUpResponse.InvalidEmailFormat(email)
            },
        )

        return SignUpResponse.Success
    }
}

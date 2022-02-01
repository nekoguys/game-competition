package ru.nekoguys.game.web.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.SignInRequest
import ru.nekoguys.game.web.dto.SignInResponse
import ru.nekoguys.game.web.dto.SignUpRequest
import ru.nekoguys.game.web.dto.SignUpResponse
import ru.nekoguys.game.web.security.jwt.JwtProvider
import ru.nekoguys.game.web.util.toBadRequestResponse
import ru.nekoguys.game.web.util.toOkResponse

@Service
class AuthService(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun signIn(request: SignInRequest): ResponseEntity<SignInResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        ).awaitFirstOrNull() ?: return SignInResponse.InvalidCredentials.toBadRequestResponse()

        val userDetails = authentication.principal as UserDetails
        val expirationTimestamp = jwtProvider.currentExpirationTimestamp()
        val jwt = jwtProvider.generateJwtToken(authentication, expirationTimestamp)

        val response = SignInResponse.Success(
            accessToken = jwt,
            email = userDetails.username,
            authorities = userDetails.authorities,
            expirationTimestamp = expirationTimestamp,
        )
        return ResponseEntity.ok(response)
    }

    suspend fun signUp(request: SignUpRequest): ResponseEntity<SignUpResponse> {
        val email = request.email.lowercase().trim()

        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            return userAlreadyExists(email)
        }

        userRepository.create(
            email = email,
            password = passwordEncoder.encode(request.password),
            role = when {
                email.endsWith("@edu.hse.ru") -> UserRole.Student
                email.endsWith("@hse.ru") -> UserRole.Teacher
                else -> return createIncorrectEmailFormatResponse(email)
            },
        )

        return SignUpResponse.Success.toOkResponse()
    }

    private fun createIncorrectEmailFormatResponse(email: String): ResponseEntity<SignUpResponse> =
        SignUpResponse.Error("Invalid email $email. Email should end with @edu.hse.ru or @hse.ru")
            .toBadRequestResponse()

    private fun userAlreadyExists(email: String): ResponseEntity<SignUpResponse> =
        SignUpResponse.Error("User with email $email already exists!")
            .toBadRequestResponse()
}

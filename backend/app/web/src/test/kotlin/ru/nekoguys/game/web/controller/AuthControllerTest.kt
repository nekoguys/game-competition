package ru.nekoguys.game.web.controller

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.SignInRequest
import ru.nekoguys.game.web.dto.SignInResponse
import ru.nekoguys.game.web.dto.SignUpRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class AuthControllerTest @Autowired constructor(
    private val game: TestGame,
    private val webTestClient: WebTestClient,
) {
    @Test
    fun `can signup`() {
        val request = SignUpRequest(
            email = "test@hse.ru",
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/auth/signup")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isNotEmpty
    }

    @Test
    fun `can't signup twice`(): Unit = runBlocking {
        val user = game.createUser()

        val request = SignUpRequest(
            email = user.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/auth/signup")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isNotEmpty
    }

    @ParameterizedTest(name = "can signin with role {1}")
    @MethodSource("roles")
    fun `can signin with role`(
        role: UserRole,
        roleName: String,
    ): Unit = runBlocking {
        val user = game.createUser(
            role = role,
            password = TestGame.DEFAULT_PASSWORD,
        )

        val request = SignInRequest(
            email = user.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        val responseBody = webTestClient
            .post()
            .uri("/api/auth/signin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody<SignInResponse>()
            .returnResult()
            .responseBody!!

        with(responseBody) {
            assertThat(email).isEqualTo(request.email)
            assertThat(accessToken).isNotBlank
            assertThat(authorities).contains(roleName)
            assertThat(expirationTimestamp).isGreaterThan(
                LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            )
        }
    }

    @Test
    fun `can't signin with unknown email`(): Unit = runBlocking {
        val request = SignInRequest(
            email = "test@hse.ru",
            password = "here",
        )

        webTestClient
            .post()
            .uri("/api/auth/signin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `can't signin with incorrect password`(): Unit = runBlocking {
        val user = game.createUser()

        val request = SignInRequest(
            email = user.email,
            password = user.password + "oops",
        )

        webTestClient
            .post()
            .uri("/api/auth/signin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized
    }

    companion object {
        @JvmStatic
        fun roles(): List<Arguments> =
            listOf(
                Arguments.of(UserRole.Admin, "ROLE_ADMIN"),
                Arguments.of(UserRole.Teacher, "ROLE_TEACHER"),
                Arguments.of(UserRole.Student, "ROLE_STUDENT"),
            )
    }
}

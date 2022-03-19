package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.CheckGamePinRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import ru.nekoguys.game.web.util.TestGame.TestData.DEFAULT_CREATE_COMPETITION_REQUEST
import ru.nekoguys.game.web.util.TestGame.TestData.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class CompetitionControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    private lateinit var testUser: User

    @BeforeEach
    fun createUser() {
        testUser = game.createUser(email = TestGame.DEFAULT_ADMIN_EMAIL)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    @Test
    fun `create competition in draft stage`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    @Test
    fun `can create competition in draft state`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    @Test
    fun `can create competition in registration state`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.pin").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can check if competition exists`() {
        val sessionPin = game.createSession()

        val request = CheckGamePinRequest(sessionPin)

        webTestClient
            .post()
            .uri("/api/competitions/check_pin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.exists").isEqualTo(true)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can check if competition doesn't exists`() {
        val sessionPin = game.createSession()

        val request = CheckGamePinRequest(
            pin = "123$sessionPin",
        )

        webTestClient
            .post()
            .uri("/api/competitions/check_pin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.exists").isEqualTo(false)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    @Test
    fun `can update competition settings`() {
        val session = game.createAndLoadSession(
            teacher = testUser
        )
        val request = DEFAULT_CREATE_COMPETITION_REQUEST.copy(
            roundLength = 30,
        )
        webTestClient
            .post()
            .uri("/api/competitions/update_competition/${session.id}")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

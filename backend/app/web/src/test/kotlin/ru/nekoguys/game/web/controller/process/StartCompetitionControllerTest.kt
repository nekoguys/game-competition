package ru.nekoguys.game.web.controller.process

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class StartCompetitionControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    lateinit var teacher: User
    lateinit var student: User

    @BeforeEach
    fun createUsers() {
        teacher = game.createUser(UserRole.Teacher)
        student = game.createUser(UserRole.Student, "test-student@hse.ru")
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    fun `can start competition`() {
        val sessionPin = game.createSession()
        repeat(2) { game.createTeam(sessionPin) }

        webTestClient
            .get()
            .uri("/api/competition_process/$sessionPin/start_competition")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = "test-student@hse.ru", roles = ["STUDENT"])
    fun `student can't start competition`() {
        val sessionPin = game.createSession()
        repeat(2) { game.createTeam(sessionPin) }

        webTestClient
            .get()
            .uri("/api/competition_process/$sessionPin/start_competition")
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    fun `can't start draft competition`() {
        val sessionPin = game.createSession(
            request = TestGame.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST
        )

        webTestClient
            .get()
            .uri("/api/competition_process/$sessionPin/start_competition")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
    fun `can start competition twice`() {
        val sessionPin = game.createSession()
        repeat(2) { game.createTeam(sessionPin) }
        game.startCompetition(sessionPin)

        webTestClient
            .get()
            .uri("/api/competition_process/$sessionPin/start_competition")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

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
import ru.nekoguys.game.web.dto.SubmitStrategyRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class SubmitStrategyTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    lateinit var student: User

    @BeforeEach
    fun createUsers() {
        student = game.createUser(UserRole.Student, TestGame.DEFAULT_STUDENT_EMAIL)
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can submit strategy`() {
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
        }
        doSubmitStrategyRequest(sessionPin, "We wanted to win")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can view submitted strategy`() {
        val strategy = "I wanted to win"
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
            game.submitStrategy(pin, student, strategy)
        }

        webTestClient
            .get()
            .uri("/api/competition_process/$sessionPin/student_comp_info")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.strategy").isEqualTo(strategy)
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can submit strategy twice`() {
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
            game.submitStrategy(pin, student, "Previous strategy")
        }

        doSubmitStrategyRequest(sessionPin, "We wanted to win")
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can't submit strategy in incorrect session stage`() {
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin, student)
        }

        doSubmitStrategyRequest(sessionPin, "We wanted to win")
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can't submit too long strategy`() {
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin, student)
        }

        val strategy = List(100) { "We want to win" }.joinToString()

        doSubmitStrategyRequest(sessionPin, strategy)
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT", "TEACHER"])
    fun `non-member can't submit strategy`() {
        val sessionPin = game.createSession { pin ->
            game.createTeam(pin)
            game.createTeam(pin)
            game.startCompetition(pin)
            game.startRound(pin)
        }

        doSubmitStrategyRequest(sessionPin, "I wanted to win")
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    private fun doSubmitStrategyRequest(
        sessionPin: String,
        strategy: String,
    ): WebTestClient.ResponseSpec =
        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/submit_strategy")
            .bodyValue(SubmitStrategyRequest(strategy))
            .exchange()
}

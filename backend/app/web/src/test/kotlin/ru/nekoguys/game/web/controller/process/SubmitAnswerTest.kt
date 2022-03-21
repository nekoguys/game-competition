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
import ru.nekoguys.game.web.dto.SubmitAnswerRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class SubmitAnswerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    lateinit var teacher: User
    lateinit var student: User
    lateinit var sessionPin: String

    @BeforeEach
    fun createTestData() {
        teacher = game.createUser(UserRole.Teacher, TestGame.DEFAULT_TEACHER_EMAIL)
        student = game.createUser(UserRole.Student, TestGame.DEFAULT_STUDENT_EMAIL)
        sessionPin = game.createSession(teacher = teacher) { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
        }
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can submit answer`() {
        val request = SubmitAnswerRequest(
            answer = 42,
            roundNumber = 1,
        )

        doSubmitAnswerRequest(request)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can submit answer twice`() {
        game.submitAnswer(sessionPin, student, 42)
        val request = SubmitAnswerRequest(
            answer = 43,
            roundNumber = 1,
        )

        doSubmitAnswerRequest(request)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can't submit answer in incorrect round`() {
        game.submitAnswer(sessionPin, student, 42)

        val request = SubmitAnswerRequest(
            answer = 43,
            roundNumber = 2,
        )

        doSubmitAnswerRequest(request)
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["STUDENT", "TEACHER"])
    fun `non-member can't submit answer`() {
        game.submitAnswer(sessionPin, student, 100)

        val request = SubmitAnswerRequest(
            answer = 42,
            roundNumber = 1,
        )

        doSubmitAnswerRequest(request)
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    private fun doSubmitAnswerRequest(
        body: SubmitAnswerRequest,
    ): WebTestClient.ResponseSpec =
        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/submit_answer")
            .bodyValue(body)
            .exchange()
}

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
class EndRoundTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    lateinit var teacher: User
    lateinit var student: User
    lateinit var sessionPin: String

    @BeforeEach
    fun createUsers() {
        teacher = game.createUser(UserRole.Teacher, TestGame.DEFAULT_TEACHER_EMAIL)
        student = game.createUser(UserRole.Student)
        sessionPin = game.createSession(teacher) { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
        }
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `teacher can end round`() {
        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/end_round")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `teacher can't end round twice`() {
        game.endRound(sessionPin)

        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/end_round")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

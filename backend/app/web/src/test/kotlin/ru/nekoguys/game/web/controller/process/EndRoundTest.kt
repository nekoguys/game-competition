package ru.nekoguys.game.web.controller.process

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.model.CompetitionSession
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
    lateinit var session: CompetitionSession.Full

    @BeforeEach
    fun createTestData() {
        teacher = game.createUser(UserRole.Teacher, TestGame.DEFAULT_TEACHER_EMAIL)
        student = game.createUser(UserRole.Student)
        session = game.createAndLoadSession(
            teacher = teacher,
            request = TestGame.DEFAULT_CREATE_COMPETITION_REQUEST
                .copy(roundsCount = 10)
        ) { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
        }
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `teacher can end round`() {
        game.startRound(session.pin)

        webTestClient
            .post()
            .uri("/api/competition_process/${session.pin}/end_round")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `teacher can't end round twice`() {
        game.startRound(session.pin)
        game.endRound(session.pin)

        webTestClient
            .post()
            .uri("/api/competition_process/${session.pin}/end_round")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `teacher can't end more rounds than session allows`() {
        for (roundNum in 0 until session.settings.roundsCount) {
            game.startRound(session.pin)
            game.endRound(session.pin)
        }

        webTestClient
            .post()
            .uri("/api/competition_process/${session.pin}/end_round")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

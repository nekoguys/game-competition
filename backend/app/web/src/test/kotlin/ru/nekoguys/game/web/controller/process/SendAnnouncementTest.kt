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
import ru.nekoguys.game.web.dto.SendAnnouncementRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class SendAnnouncementTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    lateinit var teacher: User
    lateinit var student: User

    @BeforeEach
    fun createUsers() {
        teacher = game.createUser(UserRole.Teacher, TestGame.DEFAULT_TEACHER_EMAIL)
        student = game.createUser(UserRole.Student, TestGame.DEFAULT_STUDENT_EMAIL)
    }

    @Test
    @WithMockUser(username = TestGame.DEFAULT_TEACHER_EMAIL, roles = ["TEACHER"])
    fun `can send announcement`() {
        val sessionPin = game.createSession(teacher)
        repeat(2) { game.createTeam(sessionPin) }

        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/send_message")
            .bodyValue(
                SendAnnouncementRequest(
                    message = "Test message"
                ))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }


    @Test
    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    fun `student can't send announcement`() {
        val sessionPin = game.createSession(teacher)
        repeat(2) { game.createTeam(sessionPin) }

        webTestClient
            .post()
            .uri("/api/competition_process/$sessionPin/send_message")
            .bodyValue(
                SendAnnouncementRequest(
                    message = "Test message"
                ))
            .exchange()
            .expectStatus().isForbidden
    }
}

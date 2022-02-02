package ru.nekoguys.game.web.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.service.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST

@GameWebApplicationIntegrationTest
class CompetitionControllerTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val webTestClient: WebTestClient,
) {

    init {
        runBlocking {
            userRepository.create(
                "test@hse.ru",
                password = "1234",
                role = UserRole.Teacher,
            )
        }
    }

    @WithMockUser(
        username = "test@hse.ru",
        password = "1234",
        roles = ["TEACHER"],
    )
    @Test
    fun `create competition in draft stage`(): Unit = runBlocking {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("message")
            .exists()
    }
}

package ru.nekoguys.game.web.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest

@GameWebApplicationIntegrationTest
class GameProcessControllerTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val webTestClient: WebTestClient,
) {

    private val user = runBlocking {
        userRepository.create(
            email = "test123@hse.ru",
            password = "898",
            role = UserRole.Teacher
        )
    }

    @Test
    @WithMockUser(value = "me@admin.hse.ru", roles = ["ADMIN"])
    fun test() {
        webTestClient
            .get()
            .uri("/game/hello")
            .exchange()
            .expectBody<String>()
            .isEqualTo("Hello world!")
    }
}

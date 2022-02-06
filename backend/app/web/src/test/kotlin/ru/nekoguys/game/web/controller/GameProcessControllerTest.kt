package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.util.TestUtils

@GameWebApplicationIntegrationTest
class GameProcessControllerTest @Autowired constructor(
    private val testUtils: TestUtils,
    private val userRepository: UserRepository,
    private val webTestClient: WebTestClient,
) {

    @Test
    @WithMockUser(value = "me@admin.hse.ru", roles = ["ADMIN"])
    fun test() = testUtils.runBlockingWithRollback {
        webTestClient
            .get()
            .uri("/game/hello")
            .exchange()
            .expectBody<String>()
            .isEqualTo("Hello world!")
    }
}

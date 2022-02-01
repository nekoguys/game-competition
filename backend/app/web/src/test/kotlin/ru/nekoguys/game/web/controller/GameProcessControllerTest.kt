package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.nekoguys.game.web.GameWebApplicationTest

@GameWebApplicationTest
class GameProcessControllerTest @Autowired constructor(
    applicationContext: ApplicationContext,
) {
    private val webTestClient =
        WebTestClient
            .bindToApplicationContext(applicationContext)
            .build()

    @Test
    @WithMockUser(value = "me@admin.hse.ru", password = "1234", roles = ["ADMIN"])
    fun test() {
        webTestClient
            .get()
            .uri("/game/hello")
            .exchange()
            .expectBody<String>()
            .isEqualTo("Hello world!")
    }
}

package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import ru.nekoguys.game.web.ru.nekoguys.game.web.GameWebApplicationTest

@GameWebApplicationTest
class GameProcessControllerTest @Autowired constructor(
    applicationContext: ApplicationContext,
) {
    private val webTestClient =
        WebTestClient
            .bindToApplicationContext(applicationContext)
            .build()

    @Test
    fun test() {
        webTestClient
            .get()
            .uri("/game/hello")
            .exchange()
            .expectBody<String>()
            .isEqualTo("Hello world!")
    }
}

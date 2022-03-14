package ru.nekoguys.game.web

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureWebTestClient(timeout = "PT1H")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class GameWebApplicationIntegrationTest

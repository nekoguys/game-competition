package ru.nekoguys.game.web

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.nekoguys.game.web.util.DatabaseCleanerExtension

@SpringBootTest()
@ActiveProfiles("dev")
@AutoConfigureWebTestClient
@ExtendWith(DatabaseCleanerExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class GameWebApplicationIntegrationTest

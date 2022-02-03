package ru.nekoguys.game.persistence

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration

@DataR2dbcTest
@ContextConfiguration(classes = [GamePersistenceConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class GamePersistenceTest

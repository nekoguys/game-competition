package ru.nekoguys.game.persistence

import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration

@Retention(AnnotationRetention.RUNTIME)
@DataR2dbcTest
@ContextConfiguration(classes = [GamePersistenceConfig::class])
annotation class GamePersistenceTest

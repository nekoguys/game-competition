package ru.nekoguys.game.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration

@DataR2dbcTest
@ContextConfiguration(classes = [GamePersistenceConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class GamePersistenceTest

@Configuration
class TestGamePersistenceConfig {
    @Bean
    fun objectMapper(): ObjectMapper = jsonMapper {
        addModule(kotlinModule {
            configure(KotlinFeature.SingletonSupport, true)
        })
    }
}

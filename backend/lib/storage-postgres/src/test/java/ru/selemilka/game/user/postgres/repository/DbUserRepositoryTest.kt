package ru.selemilka.game.user.postgres.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import ru.selemilka.game.user.postgres.converters.UserReadingConverter
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole

@DataR2dbcTest
class DbUserRepositoryTest(
    @Autowired
    var userRepository: DbUserRepository
) {
    @BeforeEach
    fun setup() {
        runBlocking { userRepository.deleteAll() }
    }

    @Test
    fun `simple insertion and retrieval`() {
        val user = runBlocking { userRepository.save(DbUser(email ="kpbenua@edu.hse.ru", role = DbUserRole.ADMIN)) }
        runBlocking { userRepository.save(DbUser(email ="kpbenua@edu.hse.ru", role = DbUserRole.ADMIN)) }

        val retrievedUser = runBlocking { userRepository.findById(1) }
        assertEquals(retrievedUser, user)
    }

    @Configuration
    @EnableR2dbcRepositories
    class Config {
        @Bean
        fun customConversion() : R2dbcCustomConversions {
            return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                listOf(
                    UserReadingConverter()
                )
            )
        }
    }
}
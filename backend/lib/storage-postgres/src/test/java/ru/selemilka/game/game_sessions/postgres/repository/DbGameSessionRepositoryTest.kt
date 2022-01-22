package ru.selemilka.game.game_sessions.postgres.repository

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.reactive.TransactionalOperator
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.game_props.postgres.model.DbGameProperties
import ru.selemilka.game.game_props.postgres.repository.DbGamePropertiesRepository
import ru.selemilka.game.game_sessions.postgres.model.DbGameSession
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole
import ru.selemilka.game.user.postgres.repository.DbUserRepository
import ru.selemilka.game.user.postgres.repository.runBlockingWithRollback


@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal class DbGameSessionRepositoryTest(
    @Autowired
    val gameSessionsRepository: DbGameSessionsRepository,
    @Autowired
    val userRepository: DbUserRepository,
    @Autowired
    val gamePropertiesRepository: DbGamePropertiesRepository,
    @Autowired
    val transactionalOperator: TransactionalOperator
) {

    @Test
    fun `insert and retrieval`() {
        val user = runBlockingWithRollback(transactionalOperator) {
            userRepository.save(DbUser(email = "email", role = DbUserRole.TEACHER))
        }
        val properties = runBlockingWithRollback(transactionalOperator) {
            gamePropertiesRepository.save(
                DbGameProperties(user.id!!, "competition", null)
            )
        }
        val gameSession = runBlockingWithRollback(transactionalOperator) { gameSessionsRepository.save(DbGameSession(properties.id!!)) }
        val retrievedGameSession = runBlocking { gameSessionsRepository.findById(gameSession.id!!) }
        assertNotNull(retrievedGameSession)
        assertEquals(gameSession, retrievedGameSession)
    }
}
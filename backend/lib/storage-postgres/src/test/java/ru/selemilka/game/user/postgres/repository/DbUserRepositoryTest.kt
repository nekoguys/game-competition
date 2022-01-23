package ru.selemilka.game.user.postgres.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.selemilka.game.TestingR2dbcRepositoriesConfig
import ru.selemilka.game.user.postgres.model.DbUser
import ru.selemilka.game.user.postgres.model.DbUserRole

@DataR2dbcTest
@ContextConfiguration(classes = [TestingR2dbcRepositoriesConfig::class])
internal open class DbUserRepositoryTest(
    @Autowired
    var userRepository: DbUserRepository,
    @Autowired
    var transactionalOperator: TransactionalOperator
) {

    @Test
    open fun `simple insertion and retrieval`() {
        val user = runBlockingWithRollback(transactionalOperator) {
            userRepository.save(DbUser(email ="kpbenua@edu.hse.ru", role = DbUserRole.ADMIN))
        }
        runBlocking { userRepository.save(DbUser(email ="kpbenua@edu.hse.ru", role = DbUserRole.ADMIN)) }

        val retrievedUser = runBlocking { userRepository.findById(user.id!!) }
        assertEquals(retrievedUser, user)
    }
}

fun<T> runBlockingWithRollback(operator: TransactionalOperator, block: suspend CoroutineScope.() -> T) : T {
    return runBlocking {
        operator.executeAndAwait { it.setRollbackOnly() }
        block()
    }
}

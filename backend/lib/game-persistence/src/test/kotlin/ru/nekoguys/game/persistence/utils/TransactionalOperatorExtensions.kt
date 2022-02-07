package ru.nekoguys.game.persistence.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

fun TransactionalOperator.runBlockingWithRollback(
    block: suspend CoroutineScope.() -> Unit,
) {
    runBlocking {
        this@runBlockingWithRollback.executeAndAwait { tx ->
            tx.setRollbackOnly()
            block()
        }
    }
}

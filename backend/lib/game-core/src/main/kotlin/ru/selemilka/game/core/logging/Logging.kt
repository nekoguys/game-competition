package ru.selemilka.game.core.logging

import ru.selemilka.game.core.base.AnyAction
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import kotlin.reflect.KClass

interface LogStorage<in A : AnyAction> {
    suspend fun log(id: SessionId, action: A)
}

class LoggingProcessor<A : AnyAction>(
    override val actionClass: KClass<A>,
    private val logStorage: LogStorage<A>,
) : TypedProcessor<A, Nothing> {
    override suspend fun process(id: SessionId, action: A): List<Nothing> {
        logStorage.log(id, action)
        return emptyList()
    }
}

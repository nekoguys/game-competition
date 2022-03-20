package ru.nekoguys.game.entity.commongame.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.session.GameMessageLog
import ru.nekoguys.game.core.session.LoggedGameMessage
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.commongame.repository.CommonLogMessageRepository
import java.util.*

/**
 * Фабрика для создания объектов для записи и чтения игровых сообщений из лога игры
 */
@Service
class GameMessageLogProvider(
    private val commonLogMessageRepository: CommonLogMessageRepository,
) {
    suspend fun <P, Msg> createGameLog(
        sessionId: CommonSession.Id,
        playerClass: Class<P>,
        messageClass: Class<Msg>,
    ): GameMessageLog<P, Msg> =
        GameMessageLogImpl(
            commonLogMessageRepository = commonLogMessageRepository,
            sessionId = sessionId,
            oldMessages = commonLogMessageRepository
                .readAllMessages(sessionId, playerClass, messageClass)
                .toList(),
        )
}

suspend inline fun <reified P, reified Msg> GameMessageLogProvider.createGameLog(
    sessionId: CommonSession.Id,
): GameMessageLog<P, Msg> =
    createGameLog(sessionId, P::class.java, Msg::class.java)

private class GameMessageLogImpl<P, Msg>(
    private val commonLogMessageRepository: CommonLogMessageRepository,
    private val sessionId: CommonSession.Id,
    private val oldMessages: List<LoggedGameMessage<P, Msg>>,
) : GameMessageLog<P, Msg> {

    private val mutex = Mutex()
    private val savedMessages: MutableList<LoggedGameMessage<P, Msg>> =
        Collections.synchronizedList(ArrayList())

    override val currentSessionOffset: Int
        get() = savedMessages.size

    override suspend fun saveMessages(
        messages: List<LoggedGameMessage<P, Msg>>
    ) {
        mutex.withLock {
            savedMessages += messages
        }
        commonLogMessageRepository
            .saveMessages(sessionId, messages)
    }

    override fun readAllMessages(): Flow<LoggedGameMessage<P, Msg>> =
        flow {
            oldMessages.forEach { emit(it) }
            savedMessages.forEach { emit(it) }
        }
}

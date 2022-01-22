package ru.selemilka.game.core.session

import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameMessageImpl

data class LoggedGameMessage<out P, out T>(
    val players: Set<P>,
    val messageBody: T,
)

interface GameMessageLog<P, T> {
    suspend fun saveMessages(messages: Collection<LoggedGameMessage<P, T>>)

    fun readAllMessages(): Flow<LoggedGameMessage<P, T>>
}

internal class GameSessionWithMessageLogging<in P, in Cmd, P2, Msg>(
    private val interceptedSession: InternalGameSession<P, Cmd, P2, Msg>,
    private val messageLog: GameMessageLog<P2, Msg>,
) : InternalGameSession<P, Cmd, P2, Msg> {

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> =
        interceptedSession
            .acceptAndReturnMessages(request)
            .also { messages -> logMessages(messages) }

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) =
        interceptedSession.shareMessages(messages)

    private suspend fun logMessages(messages: Collection<GameMessage<P2, Msg>>) =
        messages
            .filter { it is GameMessageImpl<*, *> }
            .map { it.toLoggedMessage() }
            .also { messagesToLog -> messageLog.saveMessages(messagesToLog) }

    private fun GameMessage<P2, Msg>.toLoggedMessage(): LoggedGameMessage<P2, Msg> =
        LoggedGameMessage(
            players = players,
            messageBody = body,
        )

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        interceptedSession.getAllMessagesIndexed()
}

internal class GameSessionWithMessagesFromLog<in P, in Cmd, P2, Msg>(
    private val interceptedSession: InternalGameSession<P, Cmd, P2, Msg>,
    private val messageLog: GameMessageLog<P2, Msg>,
) : InternalGameSession<P, Cmd, P2, Msg> {

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> =
        interceptedSession.acceptAndReturnMessages(request)

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) =
        interceptedSession.shareMessages(messages)

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        flow {
            var messagesFromLogCount = 0

            getIndexedMessagesFromLog()
                .onEach { ++messagesFromLogCount }
                .also { emitAll(it) }

            getIndexedMessagesFromSession(startIndex = messagesFromLogCount)
                .onEach { (index, _) ->
                    if (index != messagesFromLogCount++) {
                        logger.warn("Some messages were lost")
                    }
                }
                .also { emitAll(it) }
        }

    private fun getIndexedMessagesFromLog(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        messageLog
            .readAllMessages()
            .map { GameMessage(it.players, it.messageBody) }
            .withIndex()

    private fun getIndexedMessagesFromSession(startIndex: Int): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        interceptedSession
            .getAllMessagesIndexed()
            .drop(startIndex)

    private companion object {
        private val logger = LoggerFactory.getLogger(GameSessionWithMessageLogging::class.java)
    }
}

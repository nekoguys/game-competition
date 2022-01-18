package ru.selemilka.game.core.session

import kotlinx.coroutines.flow.*
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameMessageImpl
import ru.selemilka.game.core.base.GameRule

data class LoggedGameMessage<out P, out T>(
    val players: Set<P>,
    val messageBody: T,
    val traceId: TraceId?,
)

interface GameMessageLog<P, T> {
    suspend fun saveMessages(messages: Collection<LoggedGameMessage<P, T>>)

    fun readAllMessages(): Flow<LoggedGameMessage<P, T>>
}

/**
 * Обёртка над [GameRule], которая записывает все игровые сообщения в лог
 */
internal class GameRuleWithMessageLogging<in P, in Cmd, out P2, out T>(
    private val interceptedRule: GameRule<P, Cmd, GameMessage<P2, T>>,
    private val messageLog: GameMessageLog<P2, T>,
) : GameRule<P, Cmd, GameMessage<P2, T>> {

    override suspend fun process(player: P, command: Cmd): List<GameMessage<P2, T>> =
        interceptedRule
            .process(player, command)
            .also { allMessages -> logMessages(allMessages) }

    private suspend fun logMessages(messages: List<GameMessage<P2, T>>) =
        messages
            .filter { it is GameMessageImpl<*, *> }
            .map { it.toLoggedMessage() }
            .also { messagesToLog -> messageLog.saveMessages(messagesToLog) }

    private fun GameMessage<P2, T>.toLoggedMessage(): LoggedGameMessage<P2, T> =
        LoggedGameMessage(
            players = players,
            messageBody = body,
            traceId = currentTraceIdOrNull()
        )
}

internal class GameSessionWithMessageLogging<in CmdReq : GameCommandRequest<*, *>, P2, T>(
    private val interceptedSession: GameSession<CmdReq, GameMessage<P2, T>>,
    private val messageLog: GameMessageLog<P2, T>,
) : GameSession<CmdReq, GameMessage<P2, T>> {

    override suspend fun accept(request: CmdReq) {
        interceptedSession.accept(request)
    }

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, T>>> =
        flow {
            var messagesFromLogCount = 0

            getIndexedMessagesFromLog()
                .onEach { ++messagesFromLogCount }
                .also { emitAll(it) }

            getIndexedMessagesFromSession(startIndex = messagesFromLogCount)
                .onEach { (index, _) ->
                    check(index == messagesFromLogCount++) {
                        "Some messages are lost!"
                    }
                }
                .also { emitAll(it) }
        }

    private fun getIndexedMessagesFromLog(): Flow<IndexedValue<GameMessage<P2, T>>> =
        messageLog
            .readAllMessages()
            .map { GameMessage(it.players, it.messageBody) }
            .withIndex()

    private fun getIndexedMessagesFromSession(startIndex: Int): Flow<IndexedValue<GameMessage<P2, T>>> =
        interceptedSession
            .getAllMessagesIndexed()
            .drop(startIndex)
}


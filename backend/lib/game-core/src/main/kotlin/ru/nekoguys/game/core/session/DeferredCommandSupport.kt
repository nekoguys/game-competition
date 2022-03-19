package ru.nekoguys.game.core.session

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import ru.nekoguys.game.core.CloseGameSessionRequest
import ru.nekoguys.game.core.DeferredCommandRequest
import ru.nekoguys.game.core.GameCommandRequest
import ru.nekoguys.game.core.GameMessage

internal class GameSessionWithDeferredCommands<in P, in Cmd, P2, Msg>(
    private val innerSession: InternalGameSession<P, Cmd, P2, Msg>,
) : InternalGameSession<P, Cmd, P2, Msg> {

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> {
        val maybeDeferredMessages = innerSession
            .acceptAndReturnMessages(request)

        val (deferreds, nonDeferredMessages) =
            maybeDeferredMessages.partition { it is DeferredCommandRequest<GameCommandRequest<*, *>> }
        // FAST PATH - отложенных команд нет
        if (deferreds.isEmpty()) {
            return nonDeferredMessages
        }

        val (terminalDeferreds, nonTerminalDeferreds) =
            @Suppress("UNCHECKED_CAST")
            (deferreds as List<DeferredCommandRequest<GameCommandRequest<P, Cmd>>>)
                .partition { it.request == CloseGameSessionRequest }
        // TODO: предупреждать, если игровая логика прислала больше одного запроса на закрытие
        val firstTerminalDeferred = terminalDeferreds.firstOrNull()

        val childFirstTerminalDeferred = coroutineScope {
            nonTerminalDeferreds
                .map { (request, timeoutMillis) ->
                    async {
                        delay(timeoutMillis)
                        innerSession.acceptAndReturnMessages(request)
                    }
                }
                .awaitAll()
                .asSequence()
                .flatten()
                .filterIsInstance<DeferredCommandRequest<GameCommandRequest<*, *>>>()
                .firstOrNull { it.request === CloseGameSessionRequest }
        }

        // Приоритет отдаётся первой отложенной команде на закрытие сессии
        val selectedTerminalDeferred = (firstTerminalDeferred ?: childFirstTerminalDeferred)
        return if (selectedTerminalDeferred == null) {
            nonDeferredMessages
        } else {
            return nonDeferredMessages + selectedTerminalDeferred
        }
    }

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) =
        innerSession.shareMessages(messages)

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        innerSession
            .getAllMessagesIndexed()
            .filter { it.value !is DeferredCommandRequest<*> }
}

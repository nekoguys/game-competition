package ru.selemilka.game.core.session

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.selemilka.game.core.base.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext

internal class SimpleGameSession<P, in Cmd, out Msg : GameMessage<*, *>>(
    parentScope: CoroutineScope,
    private val rule: GameRule<P, Cmd, Msg>,
    private val onClose: suspend () -> Unit,
    replay: Int,
) : GameSession<GameCommandRequest<P, Cmd>, Msg> {

    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val coroutineScope = parentScope + job
    private val resourceLockSupport = ResourceLockSupport()
    private val gameMessages = MutableSharedFlow<IndexedValue<Msg>>(replay = replay)
    private val lastMessageIndex = AtomicInteger()

    override suspend fun accept(request: GameCommandRequest<P, Cmd>) {
        if (request == CloseGameSessionRequest) {
            closeSession()
        } else {
            val terminalDeferred = acceptNonTerminal(request.player, request.command)
            if (terminalDeferred != null) {
                delay(terminalDeferred.timeoutMillis)
                closeSession()
            }
        }
    }

    /**
     * Обрабатывает команду [command] от игрока [player] и возвращает
     * первый встретившийся объект `DeferredCommandRequest<CloseGameSessionRequest>`
     * (или null, если такого нет)
     */
    private suspend fun acceptNonTerminal(
        player: P,
        command: Cmd,
    ): DeferredCommandRequest<CloseGameSessionRequest>? {
        return withContext(coroutineScope.coroutineContext) {
            val (terminalDeferreds, nonTerminalDeferrents) =
                acceptAndReturnDeferredCommands(player, command)
                    .partition { it.request === CloseGameSessionRequest }
            @Suppress("UNCHECKED_CAST")
            val terminalDeferred =
                terminalDeferreds.singleOrNull() as? DeferredCommandRequest<CloseGameSessionRequest>

            val childTerminalDeferred = nonTerminalDeferrents
                .map { (request, timeoutMillis) ->
                    async {
                        delay(timeoutMillis)
                        acceptNonTerminal(request.player, request.command)
                    }
                }
                .awaitAll()
                .firstNotNullOfOrNull { it }

            // Приоритет отдаётся первой отложенной команде на закрытие сессии
            terminalDeferred ?: childTerminalDeferred
        }
    }

    private suspend fun acceptAndReturnDeferredCommands(
        player: P,
        command: Cmd,
    ): List<DeferredCommandRequest<GameCommandRequest<P, Cmd>>> {
        val lockedResources = rule.getLocksFor(command)

        return resourceLockSupport
            .useLocks(lockedResources) {
                logger.info("Started processing command $command by player $player")
                val res = rule.process(player, command)
                logger.info("Ended processing command $command by player $player")
                res
            }
            .onEach {
                if (it is GameMessageImpl<*, *>) {
                    gameMessages.emit(it.withNextIndex())
                }
            }
            .filterIsInstance<DeferredCommandRequest<GameCommandRequest<P, Cmd>>>()
    }

    private suspend fun closeSession() {
        // Проверяем, что случайно не заблокируем свою собственную джобу
        require(coroutineContext[Job] !in job.children) {
            "Cannot close a session, because current coroutine is launched inside this game session"
        }

        job.complete()
        job.join()
        @Suppress("UNCHECKED_CAST")
        gameMessages.emit((TerminalGameMessage as Msg).withNextIndex())
        onClose()
    }

    private fun Msg.withNextIndex(): IndexedValue<Msg> =
        IndexedValue(lastMessageIndex.getAndIncrement(), this)

    override fun getAllMessagesIndexed(): Flow<IndexedValue<Msg>> =
        gameMessages
            .asSharedFlow()
            .takeWhile { it.value !is TerminalGameMessage }
            .filter { it.value is GameMessageImpl<*, *> }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(SimpleGameSession::class.java)
    }
}

private object TerminalGameMessage : InternalGameMessage<Nothing, Nothing> {
    override val players: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a receiver")

    override val body: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a message")
}

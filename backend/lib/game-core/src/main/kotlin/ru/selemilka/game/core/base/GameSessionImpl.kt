package ru.selemilka.game.core.base

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import kotlin.coroutines.coroutineContext

internal class GameSessionImpl<P, Cmd, Msg>(
    parentScope: CoroutineScope,
    private val rule: GameRule<P, Cmd, Msg>,
    private val afterClose: () -> Unit,
) : GameSession<P, Cmd, Msg> {

    private val job = SupervisorJob(parentScope.coroutineContext[Job])
    private val coroutineScope = parentScope + job
    private val resourceLockSupport = ResourceLockSupport()
    private val gameMessages = MutableSharedFlow<GameMessage<P, Msg>>(replay = Int.MAX_VALUE)

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
            .onEach { gameMessages.emit(it) }
            .filterIsInstance<DeferredCommandRequest<GameCommandRequest<P, Cmd>>>()
    }

    private suspend fun closeSession() {
        val childrenJobs = job.children.toList()
        // Проверяем, что случайно не заблокируем свою собственную джобу
        require(coroutineContext[Job] !in childrenJobs) {
            "Cannot close a session, because current coroutine is launched inside this game session"
        }

        job.children.toList().joinAll()
        job.complete()
        job.join()
        gameMessages.emit(TerminalGameMessage)
        afterClose()
    }

    override fun getAllMessages(): Flow<GameMessage<P, Msg>> =
        gameMessages
            .asSharedFlow()
            .takeWhile { it !is TerminalGameMessage }
            .filterIsInstance<GameMessageImpl<P, Msg>>()

    companion object {
        private val logger = LoggerFactory.getLogger(GameSessionImpl::class.java)
    }
}

private object TerminalGameMessage : InternalGameMessage<Nothing, Nothing> {
    override val player: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a receiver")

    override val message: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a message")
}

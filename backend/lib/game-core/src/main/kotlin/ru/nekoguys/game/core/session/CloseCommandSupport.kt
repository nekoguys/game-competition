package ru.nekoguys.game.core.session

import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import ru.nekoguys.game.core.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class CloseableGameSession<in P, in Cmd, P2, Msg>(
    private val interceptedSession: InternalGameSession<P, Cmd, P2, Msg>,
    parentContext: CoroutineContext,
    private val onClose: suspend () -> Unit,
) : InternalGameSession<P, Cmd, P2, Msg> {

    private val job = SupervisorJob(parentContext[Job])
    private val context = parentContext + job

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> {
        if (request !== CloseGameSessionRequest) {
            val messages = withContext(context) {
                interceptedSession.acceptAndReturnMessages(request)
            }

            val deferredCloseRequest: DeferredCommandRequest<*> =
                messages
                    .lastOrNull()
                    ?.let { it as? DeferredCommandRequest<*> }
                    ?.takeIf { it.request == CloseGameSessionRequest }
                    ?: return messages

            delay(deferredCloseRequest.timeoutMillis)
        }

        return acceptCloseGameSessionRequest()
    }

    private suspend fun acceptCloseGameSessionRequest(): List<Nothing> {
        // Проверяем, что случайно не заблокируем сами себя
        require(coroutineContext[Job] !in job.children) {
            "Cannot close a session, because current coroutine is launched inside this game session"
        }
        job.complete()
        job.join()

        shareMessages(listOf(TerminalGameMessage))

        onClose()

        return emptyList()
    }

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) =
        interceptedSession.shareMessages(messages)

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        interceptedSession
            .getAllMessagesIndexed()
            .takeWhile { (_, value) -> value !== TerminalGameMessage }
}

internal object TerminalGameMessage : InternalGameMessage<Nothing, Nothing> {
    override val players: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a receiver")

    override val body: Nothing
        get() = error("This GameMessage was created artificially and doesn't have a message")
}

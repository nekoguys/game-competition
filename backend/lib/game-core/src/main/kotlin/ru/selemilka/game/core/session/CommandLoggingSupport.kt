package ru.selemilka.game.core.session

import kotlinx.coroutines.flow.Flow
import ru.selemilka.game.core.base.CloseGameSessionRequest
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage

data class LoggedGameCommand<out P, out Cmd>(
    val player: P,
    val command: Cmd,
    val traceId: TraceId?,
)

interface GameCommandRequestLog<P, Cmd> {
    suspend fun saveCommand(command: LoggedGameCommand<P, Cmd>)

    fun readAllCommands(): Flow<LoggedGameCommand<P, Cmd>>
}

internal class GameSessionWithCommandLogging<in P, in Cmd, Msg : GameMessage<*, *>>(
    private val interceptedSession: GameSession<GameCommandRequest<P, Cmd>, Msg>,
    private val commandLog: GameCommandRequestLog<P, Cmd>,
) : GameSession<GameCommandRequest<P, Cmd>, Msg> {

    override suspend fun accept(request: GameCommandRequest<P, Cmd>) {
        if (request !== CloseGameSessionRequest) {
            val loggedGameCommand = LoggedGameCommand(
                player = request.player,
                command = request.command,
                traceId = currentTraceIdOrNull(),
            )
            commandLog.saveCommand(loggedGameCommand)
        }
        interceptedSession.accept(request)
    }

    override fun getAllMessagesIndexed(): Flow<IndexedValue<Msg>> =
        interceptedSession.getAllMessagesIndexed()
}

package ru.selemilka.game.core.session

import kotlinx.coroutines.flow.Flow
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage

data class LoggedGameCommand<out P, out Cmd>(
    val player: P,
    val command: Cmd,
)

interface GameCommandRequestLog<P, Cmd> {
    suspend fun saveCommand(command: LoggedGameCommand<P, Cmd>)

    fun readAllCommands(): Flow<LoggedGameCommand<P, Cmd>>
}

internal class GameSessionWithCommandLogging<in P, in Cmd, P2, Msg>(
    private val interceptedSession: InternalGameSession<P, Cmd, P2, Msg>,
    private val commandLog: GameCommandRequestLog<P, Cmd>,
) : InternalGameSession<P, Cmd, P2, Msg> {

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> =
        interceptedSession
            .acceptAndReturnMessages(request)
            .also {
                val loggedGameCommand = LoggedGameCommand(
                    player = request.player,
                    command = request.command,
                )
                commandLog.saveCommand(loggedGameCommand)
            }

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) =
        interceptedSession.shareMessages(messages)

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        interceptedSession.getAllMessagesIndexed()
}

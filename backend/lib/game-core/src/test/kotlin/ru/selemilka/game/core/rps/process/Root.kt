package ru.selemilka.game.core.rps.process

import ru.selemilka.game.core.base.Announcement
import ru.selemilka.game.core.base.Command
import ru.selemilka.game.core.rps.Session

/**
 * Команды отправляются игроками
 * Игрок однозначно определяется своим именем и сессией, в которую он отправил команду
 */
data class RpsPlayer(
    val name: String,
    val session: Session,
)

/**
 * Команды в камень-ножницы-бумаге
 * Их отправляют только игроки [RpsPlayer], других отправителей нет
 */
sealed interface RpsCommand : Command<RpsPlayer> {
    object JoinGame : RpsCommand
    data class MakeTurn(val turn: Turn) : RpsCommand
}

/**
 * Все сообщения в камень-ножницы-бумаге - подтипы [RpsMessage]
 */
interface RpsMessage

/**
 * Сообщения рассылаются:
 * * конкретному игроку [RpsPlayer]
 * * всем игрокам в сессии
 */
sealed interface RpsAnnouncement<out Msg : RpsMessage> : Announcement<Msg> {
    data class ToPlayer<Msg : RpsMessage>(
        val player: RpsPlayer,
        override val message: Msg,
    ) : RpsAnnouncement<Msg>

    data class ToSession<Msg : RpsMessage>(
        val session: Session,
        override val message: Msg,
    ) : RpsAnnouncement<Msg>

    data class ToAll<Msg : RpsMessage>(
        override val message: Msg,
    ) : RpsAnnouncement<Msg>
}

class RpsRootProcessor(
    private val joinGameProcessor: RpsJoinGameProcessor,
) {
    suspend fun process(
        player: RpsPlayer,
        command: RpsCommand,
    ): List<RpsAnnouncement<RpsMessage>> =
        when (command) {
            is RpsCommand.JoinGame -> joinGameProcessor.joinGame(player)
            is RpsCommand.MakeTurn -> makeTurnProcessor.makeTurn(player, command.turn)
            else -> error("Unknown command $command")
        }
}

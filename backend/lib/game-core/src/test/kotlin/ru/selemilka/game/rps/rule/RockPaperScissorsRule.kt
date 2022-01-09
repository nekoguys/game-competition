package ru.selemilka.game.rps.rule

import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.CloseGameSessionRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameRule
import ru.selemilka.game.core.base.LockedResources
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsStage
import ru.selemilka.game.rps.model.Turn
import kotlin.reflect.KClass


/**
 * Команды в камень-ножницы-бумаге
 * Их отправляют только игроки [RpsPlayer], других отправителей нет.
 *
 * Все наследники [RpsCommand] перечислены здесь,
 * потому что нам нужно заранее знать типы всех команд, чтобы их обрабатывать
 */
sealed interface RpsCommand {
    /**
     * Команда, отправляемая игроком для присоединения к игре.
     */
    object JoinGame : RpsCommand

    /**
     * Команда, отправляемая игроком для того, чтобы сходить в этом раунде
     */
    data class SubmitAnswer(val turn: Turn) : RpsCommand

    /**
     * Команда, отправляемая правилами [ru.selemilka.game.rps.rule.RpsJoinGameRule]
     * и [ru.selemilka.game.rps.rule.RpsSubmitAnswerRule], чтобы поменять стадию игры.
     *
     * Обрати внимание на тип игрока [RpsPlayer.Internal] - эту команду не могут отправлять люди
     */
    data class ChangeStage(val newStage: RpsStage) : RpsCommand
}

/**
 * Все сообщения в камень-ножницы-бумаге - подтипы [RpsMessage].
 */
sealed interface RpsMessage {
    @JvmInline
    value class JoinGame(val inner: JoinGameMessage) : RpsMessage

    @JvmInline
    value class SubmitAnswer(val inner: SubmitAnswerMessage) : RpsMessage

    @JvmInline
    value class StageChanged(val inner: ChangeStageMessage) : RpsMessage

    data class UnableRequestCommand(
        val player: RpsPlayer,
        val expectedClass: KClass<out RpsPlayer>,
    ) : RpsMessage
}

/**
 * В процессе обработки команды нам может быть нужно выполнить ещё команды.
 * В этом случае можно вернуть отдельное сообщение [CloseGameSessionRequest]
 */
@Service
class RpsRootRule(
    private val joinGameRule: RpsJoinGameRule,
    private val submitAnswerRule: RpsSubmitAnswerRule,
    private val changeStageRule: ChangeStageRule,
) : GameRule<RpsPlayer, RpsCommand, RpsMessage> {

    override suspend fun getLocksFor(command: RpsCommand): LockedResources =
        when (command) {
            is RpsCommand.JoinGame -> joinGameRule.getLocksFor(command)
            is RpsCommand.SubmitAnswer -> submitAnswerRule.getLocksFor(command)
            is RpsCommand.ChangeStage -> changeStageRule.getLocksFor(command)
        }

    override suspend fun process(
        player: RpsPlayer,
        command: RpsCommand,
    ): List<GameMessage<RpsPlayer, RpsMessage>> =
        when (command) {
            is RpsCommand.JoinGame -> joinGameRule.process(player, command)
            is RpsCommand.SubmitAnswer -> submitAnswerRule.process(player, command)
            is RpsCommand.ChangeStage -> changeStageRule.process(player, command)
        }
}

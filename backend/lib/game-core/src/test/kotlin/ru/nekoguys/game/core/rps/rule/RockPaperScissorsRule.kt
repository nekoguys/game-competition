package ru.nekoguys.game.core.rps.rule

import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.CloseGameSessionRequest
import ru.nekoguys.game.core.ResourceLocks
import ru.nekoguys.game.core.rps.RpsGameMessage
import ru.nekoguys.game.core.rps.RpsGameRule
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsStage
import ru.nekoguys.game.core.rps.model.Turn


/**
 * Команды в камень-ножницы-бумаге
 * Их отправляют только игроки [RpsPlayer], других отправителей нет.
 *
 * Все наследники [RpsCommand] перечислены здесь,
 * потому что нам нужно заранее знать типы всех команд, чтобы их обрабатывать
 */
@Serializable
sealed class RpsCommand {
    /**
     * Команда, отправляемая игроком для присоединения к игре.
     */
    @Serializable
    object JoinGame : RpsCommand()

    /**
     * Команда, отправляемая игроком для того, чтобы сходить в этом раунде
     */
    @Serializable
    data class SubmitAnswer(val turn: Turn) : RpsCommand()

    /**
     * Команда, отправляемая правилами [RpsJoinGameRule]
     * и [RpsSubmitAnswerRule], чтобы поменять стадию игры.
     *
     * Обрати внимание на тип игрока [RpsPlayer.Internal] - эту команду не могут отправлять люди
     */
    @Serializable
    data class ChangeStage(val newStage: RpsStage) : RpsCommand()
}

/**
 * Все сообщения в камень-ножницы-бумаге - подтипы [RpsMessage].
 */
@Serializable
sealed class RpsMessage

/**
 * В процессе обработки команды нам может быть нужно выполнить ещё команды.
 * В этом случае можно вернуть отдельное сообщение [CloseGameSessionRequest]
 */
@Service
class RpsRootRule(
    private val joinGameRule: RpsJoinGameRule,
    private val submitAnswerRule: RpsSubmitAnswerRule,
    private val changeStageRule: ChangeStageRule,
) : RpsGameRule<RpsPlayer, RpsCommand, RpsMessage> {

    override suspend fun getLocksFor(
        command: RpsCommand,
    ): ResourceLocks =
        when (command) {
            is RpsCommand.JoinGame -> joinGameRule.getLocksFor(command)
            is RpsCommand.SubmitAnswer -> submitAnswerRule.getLocksFor(command)
            is RpsCommand.ChangeStage -> changeStageRule.getLocksFor(command)
        }

    override suspend fun process(
        player: RpsPlayer,
        command: RpsCommand,
    ): List<RpsGameMessage<RpsMessage>> =
        when (command) {
            is RpsCommand.JoinGame -> {
                require(player is RpsPlayer.Human) {
                    "Expected player of type ${RpsPlayer.Human::class}, but got $player"
                }
                joinGameRule.process(player, command)
            }

            is RpsCommand.SubmitAnswer -> {
                require(player is RpsPlayer.Human) {
                    "Expected player of type ${RpsPlayer.Human::class}, but got $player"
                }
                submitAnswerRule.process(player, command)
            }

            is RpsCommand.ChangeStage -> {
                require(player is RpsPlayer.Internal) {
                    "Expected player of type ${RpsPlayer.Human::class}, but got $player"
                }
                changeStageRule.process(player, command)
            }
        }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RpsRootRule::class.java)
    }
}

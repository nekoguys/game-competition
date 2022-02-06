package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.*

sealed class CompetitionCommand {
    data class CreateTeam(
        val teamName: String,
    ) : CompetitionCommand()

    data class JoinTeam(
        val teamName: String,
    ) : CompetitionCommand()

    data class ChangeStageCommand(
        val from: CompetitionStage,
        val to: CompetitionStage,
    ) : CompetitionCommand()
}

sealed class CompetitionMessage

typealias CompGameMessage<Msg> = GameMessage<CompetitionTeam.Id, Msg>

typealias CompetitionRule<P, Cmd, Msg> = GameRule<P, Cmd, CompetitionTeam.Id, Msg>

@Service
class CompetitionRootRule(
    private val createTeamRule: CompetitionCreateTeamRule,
    private val changeStageRule: CompetitionChangeStageRule,
    private val joinTeamRule: CompetitionJoinTeamRule,
) : CompetitionRule<CompetitionBasePlayer, CompetitionCommand, CompetitionMessage> {

    override suspend fun process(
        player: CompetitionBasePlayer,
        command: CompetitionCommand,
    ): List<CompGameMessage<CompetitionMessage>> =
        when (command) {
            is CompetitionCommand.CreateTeam -> {
                if (player !is CompetitionPlayer.Unknown) {
                    val msg = if (player is CompetitionPlayer.TeamCaptain) {
                        "${player.user.email} is Captain and is in another team already"
                    } else {
                        "Player $player must not be a member of any team"
                    }
                    throw CompetitionProcessException(msg)
                }
                createTeamRule.process(player, command)
            }

            is CompetitionCommand.JoinTeam -> {
                if (player !is CompetitionPlayer.Unknown) {
                    val msg = if (player is CompetitionPlayer) {
                        "This user is in another team already"
                    } else {
                        "Player $player must not be a member of any team"
                    }
                    throw CompetitionProcessException(msg)
                }
                joinTeamRule.process(player, command)
            }

            is CompetitionCommand.ChangeStageCommand -> {
                require(player is InternalPlayer) { "Player $player must be internal" }
                changeStageRule.process(player, command)
            }
        }
}

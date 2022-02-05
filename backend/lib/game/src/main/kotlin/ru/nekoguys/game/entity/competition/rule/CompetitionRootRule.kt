package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
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
                require(player is CompetitionPlayer.Unknown) { "Player $player must be unknown" }
                createTeamRule.process(player, command)
            }
            is CompetitionCommand.ChangeStageCommand -> {
                require(player is InternalPlayer) { "Player $player must be internal" }
                changeStageRule.process(player, command)
            }
            is CompetitionCommand.JoinTeam -> {
                require(player is CompetitionPlayer.Unknown) { "Player $player must be unknown" }
                joinTeamRule.process(player, command)
            }
        }
}

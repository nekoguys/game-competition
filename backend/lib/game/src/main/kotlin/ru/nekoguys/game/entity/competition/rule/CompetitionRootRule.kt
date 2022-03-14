package ru.nekoguys.game.entity.competition.rule

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import ru.nekoguys.game.entity.competition.CompetitionProcessException
import ru.nekoguys.game.entity.competition.model.*

sealed class CompetitionCommand {
    data class CreateTeam(
        val teamName: String,
        val password: String,
    ) : CompetitionCommand()

    data class JoinTeam(
        val teamName: String,
        val password: String,
    ) : CompetitionCommand()

    data class ChangeStageCommand(
        val from: CompetitionStage,
        val to: CompetitionStage,
    ) : CompetitionCommand()
}

/**
 * В теории, с использованием jackson-module-kotlin подтипы указывать не нужно
 * Но эта фича почему-то не работает
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(CompetitionCreateTeamMessage::class),
    JsonSubTypes.Type(CompetitionJoinTeamMessage::class),
    JsonSubTypes.Type(CompetitionChangeStageRule::class),
)
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
            is CompetitionCommand.CreateTeam ->
                createTeam(player, command)

            is CompetitionCommand.JoinTeam ->
                joinTeam(player, command)

            is CompetitionCommand.ChangeStageCommand ->
                changeStage(player, command)
        }

    private suspend fun createTeam(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.CreateTeam,
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> =
        when (player) {
            is CompetitionPlayer.TeamCaptain ->
                throw CompetitionProcessException(
                    "${player.user.email} is Captain and is in another team already"
                )

            is CompetitionPlayer.TeamMember ->
                throw CompetitionProcessException(
                    "Player ${player.user.email} must not be a member of any team"
                )

            is CompetitionPlayer.Unknown ->
                createTeamRule.process(player, command)

            else -> error("Got unexpected player $player")
        }

    private suspend fun joinTeam(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.JoinTeam,
    ): List<CompGameMessage<CompetitionJoinTeamMessage>> =
        when (player) {
            is CompetitionPlayer.Student ->
                throw CompetitionProcessException(
                    "This user is in another team already"
                )

            is CompetitionPlayer.Teacher ->
                throw CompetitionProcessException(
                    "It is forbidden to play with yourself"
                )

            is CompetitionPlayer.Unknown ->
                joinTeamRule.process(player, command)

            else -> error("Got unexpected player $player")
        }

    private suspend fun changeStage(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.ChangeStageCommand,
    ): List<CompGameMessage<CompetitionMessage>> {
        require(player is InternalPlayer) { "Player $player must be internal" }
        return changeStageRule.process(player, command)
    }
}

package ru.nekoguys.game.entity.competition.rule

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import ru.nekoguys.game.entity.commongame.repository.CommonSessionRepository
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.processError

sealed class CompetitionCommand {
    data class CreateTeam(
        val teamName: String,
        val password: String,
    ) : CompetitionCommand()

    data class JoinTeam(
        val teamName: String,
        val password: String,
    ) : CompetitionCommand()

    data class ChangeStage(
        val from: CompetitionStage,
        val to: CompetitionStage,
    ) : CompetitionCommand()

    data class SubmitAnswer(
        val answer: Long,
        val currentRound: Int,
    ) : CompetitionCommand()

    data class ChangeCompetitionSettings(
        val newSettings: CompetitionSettings,
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
    JsonSubTypes.Type(CompetitionStageChangedMessage::class),
    JsonSubTypes.Type(CompetitionAnswerSubmittedMessage::class),
)
sealed class CompetitionMessage

typealias CompGameMessage<Msg> = GameMessage<CompetitionTeam.Id, Msg>

typealias CompetitionRule<P, Cmd, Msg> = GameRule<P, Cmd, CompetitionTeam.Id, Msg>

@Service
class CompetitionRootRule(
    private val commonSessionRepository: CommonSessionRepository,
    private val createTeamRule: CompetitionCreateTeamRule,
    private val changeStageRule: CompetitionChangeStageRule,
    private val joinTeamRule: CompetitionJoinTeamRule,
    private val submitAnswerRule: CompetitionSubmitAnswerRule,
    private val competitionChangeSettingsRule: CompetitionChangeSettingsRule,
) : CompetitionRule<CompetitionBasePlayer, CompetitionCommand, CompetitionMessage> {

    override suspend fun process(
        player: CompetitionBasePlayer,
        command: CompetitionCommand,
    ): List<CompGameMessage<CompetitionMessage>> {
        if (player is BannedCompetitionPlayer) {
            processError("You were banned!")
        }

        val result = when (command) {
            is CompetitionCommand.CreateTeam ->
                createTeam(player, command)

            is CompetitionCommand.JoinTeam ->
                joinTeam(player, command)

            is CompetitionCommand.ChangeStage ->
                changeStage(player, command)

            is CompetitionCommand.SubmitAnswer ->
                submitAnswer(player, command)

            is CompetitionCommand.ChangeCompetitionSettings ->
                changeCompetitionSettings(player, command)
        }

        commonSessionRepository
            .updateLastModifiedTime(player.sessionId)

        return result
    }

    private suspend fun createTeam(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.CreateTeam,
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> =
        when (player) {
            is CompetitionPlayer.TeamCaptain ->
                processError(
                    "${player.user.email} is Captain and is in another team already"
                )

            is CompetitionPlayer.TeamMember ->
                processError(
                    "Player ${player.user.email} must not be a member of any team"
                )

            is CompetitionPlayer.Unknown ->
                createTeamRule.process(player, command)

            else -> processError("Got unexpected player $player")
        }

    private suspend fun joinTeam(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.JoinTeam,
    ): List<CompGameMessage<CompetitionJoinTeamMessage>> =
        when (player) {
            is CompetitionPlayer.Student ->
                processError("This user is in another team already")

            is CompetitionPlayer.Teacher ->
                processError("It is forbidden to play with yourself")

            is CompetitionPlayer.Unknown ->
                joinTeamRule.process(player, command)

            else -> processError("Got unexpected player $player")
        }

    private suspend fun changeStage(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.ChangeStage,
    ): List<CompGameMessage<CompetitionMessage>> {
        if (player !is InternalPlayer) {
            processError("Player $player must be internal")
        }
        return changeStageRule.process(player, command)
    }

    private suspend fun submitAnswer(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.SubmitAnswer,
    ): List<CompGameMessage<CompetitionMessage>> {
        if (player !is CompetitionPlayer.TeamCaptain) {
            processError("User $player must be a captain")
        }
        return submitAnswerRule.process(player, command)
    }

    private suspend fun changeCompetitionSettings(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.ChangeCompetitionSettings,
    ): List<CompGameMessage<CompetitionMessage>> {
        require(player is CompetitionPlayer.Teacher) { "Player $player must be teacher" }
        return competitionChangeSettingsRule.process(player, command)
    }
}

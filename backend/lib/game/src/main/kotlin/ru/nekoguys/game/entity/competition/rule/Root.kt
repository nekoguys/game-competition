package ru.nekoguys.game.entity.competition.rule

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import ru.nekoguys.game.core.LockableResource
import ru.nekoguys.game.core.ResourceLocks
import ru.nekoguys.game.entity.commongame.repository.CommonSessionRepository
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.service.processError

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

    data class ChangeCompetitionSettings(
        val newSettings: CompetitionSettings,
    ) : CompetitionCommand()

    object Start : CompetitionCommand()

    object StartRound : CompetitionCommand()

    object EndCurrentRound : CompetitionCommand()

    data class SubmitAnswer(
        val answer: Int,
        val currentRound: Int,
    ) : CompetitionCommand()

    data class BanTeams(
        val teamIds: Collection<CompetitionTeam.Id>,
        val reason: String,
    ) : CompetitionCommand()

    data class SendAnnouncement(
        val announcement: String,
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
    JsonSubTypes.Type(CompetitionTeamBannedMessage::class),
    JsonSubTypes.Type(CompetitionPriceChangeMessage::class),
    JsonSubTypes.Type(CompetitionRoundResultsMessage::class),
)
sealed class CompetitionMessage

typealias CompGameMessage<Msg> = GameMessage<CompetitionTeam.Id, Msg>

typealias CompetitionRule<P, Cmd, Msg> = GameRule<P, Cmd, CompetitionTeam.Id, Msg>

@Component
class CompetitionRootRule(
    private val commonSessionRepository: CommonSessionRepository,
    private val createTeamRule: CompetitionCreateTeamRule,
    private val changeStageRule: CompetitionChangeStageRule,
    private val joinTeamRule: CompetitionJoinTeamRule,
    private val submitAnswerRule: CompetitionSubmitAnswerRule,
    private val changeSettingsRule: CompetitionChangeSettingsRule,
    private val startRoundRule: CompetitionStartRoundRule,
    private val endRoundRule: CompetitionEndRoundRule,
    private val startRule: CompetitionStartRule,
    private val banTeamRule: CompetitionBanTeamRule,
    private val sendAnnouncementRule: SendAnnouncement,
) : CompetitionRule<CompetitionBasePlayer, CompetitionCommand, CompetitionMessage> {

    object ChangeStageLock : LockableResource()

    override suspend fun getLocksFor(
        command: CompetitionCommand,
    ): ResourceLocks =
        when (command) {
            is CompetitionCommand.ChangeStage -> ResourceLocks(
                shared = sortedSetOf(),
                unique = sortedSetOf(ChangeStageLock)
            )
            else -> super.getLocksFor(command)
        }

    override suspend fun process(
        player: CompetitionBasePlayer,
        command: CompetitionCommand,
    ): List<CompGameMessage<CompetitionMessage>> {
        if (player is CompetitionPlayer.Student && player.banRoundNumber != null) {
            processError("You were banned in round ${player.banRoundNumber}!")
        }

        val result = when (command) {
            is CompetitionCommand.CreateTeam ->
                createTeamRule.createTeam(player, command)
            is CompetitionCommand.JoinTeam ->
                joinTeamRule.joinTeam(player, command)
            is CompetitionCommand.ChangeStage ->
                changeStageRule.changeStage(player, command)
            is CompetitionCommand.Start ->
                startRule.start(player)
            is CompetitionCommand.StartRound ->
                startRoundRule.startRound(player)
            is CompetitionCommand.EndCurrentRound ->
                endRoundRule.endRound(player)
            is CompetitionCommand.SubmitAnswer ->
                submitAnswerRule.submitAnswer(player, command)
            is CompetitionCommand.BanTeams ->
                banTeamRule.banTeam(player, command)
            is CompetitionCommand.ChangeCompetitionSettings ->
                changeSettingsRule.changeSettings(player, command)
            is CompetitionCommand.SendAnnouncement ->
                sendAnnouncementRule.sendAnnouncement(player, command)
        }

        commonSessionRepository
            .updateLastModifiedTime(player.sessionId)

        return result
    }

}

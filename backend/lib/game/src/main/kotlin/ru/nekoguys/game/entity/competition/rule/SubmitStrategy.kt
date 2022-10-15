package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.service.processError
import java.time.LocalDateTime


@Component
class CompetitionSubmitStrategyRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionRoundRepository: CompetitionRoundRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) : CompetitionRule<CompetitionPlayer.TeamCaptain, CompetitionCommand.SubmitStrategy, Nothing> {

    override suspend fun process(
        player: CompetitionPlayer.TeamCaptain,
        command: CompetitionCommand.SubmitStrategy
    ): List<CompGameMessage<Nothing>> {
        checkStage(player.sessionId)

        val processedStrategy = command.strategy.trim()
        if (processedStrategy.length >= MAX_STRATEGY_LENGTH) {
            processError("Strategy length must be less than $MAX_STRATEGY_LENGTH symbols")
        }

        val currentTeam = competitionTeamRepository.load(player.teamId)
        competitionTeamRepository.update(
            from = currentTeam,
            to = currentTeam.copy(
                strategy = command.strategy
            )
        )

        // Игрокам не отправляются сообщения
        return emptyList()
    }

    private suspend fun checkStage(
        sessionId: CommonSession.Id,
    ) {
        val session = competitionSessionRepository
            .load(
                sessionId,
                CompetitionSession.WithStage,
                CompetitionSession.WithSettings,
            )

        when (session.stage) {
            is CompetitionStage.Draft, is CompetitionStage.Registration ->
                processError("Tried to submit in not started game")

            is CompetitionStage.Ended -> {
                val lastRoundNumber = session.settings.roundsCount
                val lastRound = competitionRoundRepository
                    .find(sessionId, lastRoundNumber)
                    ?: error("Round $lastRoundNumber not found in session $sessionId")

                val endTime = (lastRound as CompetitionRound.Ended).endTime
                val now = LocalDateTime.now()
                if (endTime.plusMinutes(MINUTES_TO_WAIT) < now) {
                    processError("Can't submit strategy, it's too late")
                }
            }
            else -> {}
        }
    }

    companion object {
        const val MINUTES_TO_WAIT = 15L
        const val MAX_STRATEGY_LENGTH = 300
    }
}

suspend fun CompetitionSubmitStrategyRule.submitStrategy(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.SubmitStrategy,
): List<CompGameMessage<CompetitionMessage>> {
    if (player !is CompetitionPlayer.TeamCaptain) {
        processError("User $player must be a captain")
    }
    return process(player, command)
}

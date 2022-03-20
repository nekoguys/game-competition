package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.core.util.defer
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundAnswerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.service.RoundResultsCalculator
import ru.nekoguys.game.entity.competition.service.processError

data class CompetitionPriceChangeMessage(
    val roundNumber: Int,
    val price: Double,
) : CompetitionMessage()

data class CompetitionRoundResultsMessage(
    val roundNumber: Int,
    val price: Double,
    val income: Double,
) : CompetitionMessage()

@Component
class CompetitionEndRoundRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val roundResultsCalculator: RoundResultsCalculator,
    private val competitionRoundAnswerRepository: CompetitionRoundAnswerRepository,
) : CompetitionRule<CompetitionPlayer.Teacher, CompetitionCommand.EndCurrentRound, CompetitionMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Teacher,
        command: CompetitionCommand.EndCurrentRound
    ): List<GameMessage<CompetitionTeam.Id, CompetitionMessage>> {
        val session = competitionSessionRepository
            .load(
                player.sessionId,
                CompetitionSession.WithStage,
                CompetitionSession.WithRounds,
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeamIds,
            )

        val stage = session.stage
        if (stage !is CompetitionStage.InProcess) {
            processError("Can't end round, when session is in stage ${stage.name}")
        }
        val currentRoundNumber = stage.round

        val roundAnswers = session
            .rounds
            .maxByOrNull { it.roundNumber }
            ?.answers
            .orEmpty()

        val (results, bannedTeamsIds, price) = roundResultsCalculator
            .calculateResults(roundAnswers)

        for (answer in roundAnswers) {
            val newAnswer = answer.withIncome(results.getValue(answer.teamId))
            competitionRoundAnswerRepository.update(newAnswer)
        }

        return buildResponse {
            if (bannedTeamsIds.isNotEmpty()) {
                defer(
                    fromPlayer = InternalPlayer(sessionId = player.sessionId),
                    command = CompetitionCommand.BanTeams(
                        teamIds = bannedTeamsIds,
                        reason = "too much loss",
                    )
                )
            }

            defer(
                fromPlayer = InternalPlayer(sessionId = player.sessionId),
                command = CompetitionCommand.ChangeStage(
                    from = CompetitionStage.InProcess(currentRoundNumber),
                    to = CompetitionStage.WaitingStart(currentRoundNumber + 1)
                )
            )

            session.teamIds {
                +CompetitionPriceChangeMessage(
                    roundNumber = currentRoundNumber,
                    price = price
                )
            }

            for (teamId in session.teamIds) {
                teamId {
                    +CompetitionRoundResultsMessage(
                        roundNumber = currentRoundNumber,
                        price = price,
                        income = results[teamId] ?: -1.0,
                    )
                }
            }
        }
    }
}

suspend fun CompetitionEndRoundRule.endRound(
    player: CompetitionBasePlayer,
): List<GameMessage<CompetitionTeam.Id, CompetitionMessage>> {
    if (player !is CompetitionPlayer.Teacher) {
        processError("Player $player must be teacher")
    }
    return process(player, CompetitionCommand.EndCurrentRound)
}

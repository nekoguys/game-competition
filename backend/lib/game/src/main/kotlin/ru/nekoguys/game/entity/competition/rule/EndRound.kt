package ru.nekoguys.game.entity.competition.rule

import kotlinx.coroutines.flow.toList
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
                CompetitionSession.WithSettings,
                CompetitionSession.WithTeamIds,
            )

        val stage = session.stage
        if (stage !is CompetitionStage.InProcess) {
            processError("Can't end round, when session is in stage ${stage.name}")
        }
        val currentRoundNumber = stage.round

        val roundAnswers = competitionRoundAnswerRepository
            .findAll(session.id, currentRoundNumber)
            .toList()

        val (incomes, bannedTeamsIds, price) = RoundResultsCalculator
            .calculateResults(session.settings, roundAnswers)

        for (answer in roundAnswers) {
            val newAnswer = answer.withIncome(incomes.getValue(answer.teamId))
            competitionRoundAnswerRepository.update(newAnswer)
        }

        return buildResponse {
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
                        income = incomes.getValue(teamId),
                    )
                }
            }

            if (bannedTeamsIds.isNotEmpty()) {
                defer(
                    fromPlayer = InternalPlayer(sessionId = player.sessionId),
                    command = CompetitionCommand.BanTeams(
                        teamIds = bannedTeamsIds,
                        reason = "command lost too much",
                    )
                )
            }

            defer(
                fromPlayer = InternalPlayer(sessionId = player.sessionId),
                command = CompetitionCommand.ChangeStage(
                    from = CompetitionStage.InProcess(currentRoundNumber),
                    to = if (currentRoundNumber < session.settings.roundsCount) {
                        CompetitionStage.WaitingStart(currentRoundNumber + 1)
                    } else {
                        CompetitionStage.Ended
                    },
                )
            )
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

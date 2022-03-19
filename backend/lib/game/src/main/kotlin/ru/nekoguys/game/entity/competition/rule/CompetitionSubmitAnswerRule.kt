package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundAnswerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.service.processError


data class CompetitionAnswerSubmittedMessage(
    val roundNumber: Int,
    val answer: Int,
) : CompetitionMessage()

@Component
class CompetitionSubmitAnswerRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionRoundAnswerRepository: CompetitionRoundAnswerRepository,
) : CompetitionRule<CompetitionPlayer.TeamCaptain, CompetitionCommand.SubmitAnswer, CompetitionAnswerSubmittedMessage> {

    override suspend fun process(
        player: CompetitionPlayer.TeamCaptain,
        command: CompetitionCommand.SubmitAnswer
    ): List<CompGameMessage<CompetitionAnswerSubmittedMessage>> {
        val currentStage = competitionSessionRepository
            .load(player.sessionId, CompetitionSession.WithStage)
            .stage

        if (currentStage !is CompetitionStage.InProcess) {
            processError("Tried to submit in not started game")
        } else if (currentStage.round != command.currentRound) {
            processError("Tried to submit answer in invalid round")
        }

        val currentRoundAnswer = competitionRoundAnswerRepository
            .find(player.sessionId, player.teamId, currentStage.round)
        if (currentRoundAnswer != null) {
            processError("You've already submitted your answer!")
        }

        val newAnswer = CompetitionRoundAnswer.Impl(
            sessionId = player.sessionId,
            teamId = player.teamId,
            roundNumber = currentStage.round,
            value = command.answer,
        )
        competitionRoundAnswerRepository.save(newAnswer)

        return buildResponse {
            player.teamId {
                +CompetitionAnswerSubmittedMessage(
                    roundNumber = currentStage.round,
                    answer = newAnswer.value,
                )
            }
        }
    }
}

suspend fun CompetitionSubmitAnswerRule.submitAnswer(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.SubmitAnswer,
): List<CompGameMessage<CompetitionMessage>> {
    if (player !is CompetitionPlayer.TeamCaptain) {
        processError("User $player must be a captain")
    }
    return process(player, command)
}

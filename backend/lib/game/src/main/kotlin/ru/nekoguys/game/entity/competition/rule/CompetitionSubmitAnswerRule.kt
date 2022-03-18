package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.processError
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundAnswerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.competition.repository.load


data class CompetitionAnswerSubmittedMessage(
    val teamNumberInGame: Int,
    val roundNumber: Int,
    val answer: Long,
) : CompetitionMessage()

@Component
class CompetitionSubmitAnswerRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionRoundAnswerRepository: CompetitionRoundAnswerRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
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

        val currentTeam = competitionTeamRepository.load(player.teamId)
        return buildResponse {
            player.teamId {
                +CompetitionAnswerSubmittedMessage(
                    teamNumberInGame = currentTeam.numberInGame,
                    roundNumber = currentStage.round,
                    answer = newAnswer.value,
                )
            }
        }
    }
}

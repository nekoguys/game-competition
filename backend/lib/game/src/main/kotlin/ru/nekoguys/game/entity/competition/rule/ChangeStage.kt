package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.model.InternalPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import java.time.LocalDateTime

data class CompetitionStageChangedMessage(
    val from: CompetitionStage,
    val to: CompetitionStage,
    val timeStamp: LocalDateTime,
    val roundLength: Int,
) : CompetitionMessage()

@Component
class CompetitionChangeStageRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
    private val competitionRoundRepository: CompetitionRoundRepository,
) : CompetitionRule<InternalPlayer, CompetitionCommand.ChangeStage, CompetitionMessage> {

    override suspend fun process(
        player: InternalPlayer,
        command: CompetitionCommand.ChangeStage,
    ): List<CompGameMessage<CompetitionMessage>> {
        val session = competitionSessionRepository
            .load(
                player.sessionId,
                CompetitionSession.WithStage,
                CompetitionSession.WithTeamIds,
                CompetitionSession.WithSettings,
            )
        checkCurrentStage(session, command)

        competitionSessionRepository.update(
            from = session,
            to = session.copy(
                stage = command.to
            )
        )

        if (command.from is CompetitionStage.InProcess) {
            competitionRoundRepository.endRound(
                sessionId = session.id,
                roundNumber = command.from.round
            )
        }

        if (command.to is CompetitionStage.InProcess) {
            competitionRoundRepository.startRound(
                sessionId = session.id,
                roundNumber = command.to.round
            )
        }

        val message = CompetitionStageChangedMessage(
            from = command.from,
            to = command.to,
            timeStamp = LocalDateTime.now(),
            roundLength = session.settings.roundLength,
        )
        return buildResponse {
            session.teamIds {
                add(message)
            }
        }
    }

    private fun checkCurrentStage(
        session: CompetitionSession.WithStage,
        command: CompetitionCommand.ChangeStage
    ) {
        val currentStage = session.stage

        if (session.stage != command.from) {
            with(command) {
                error(
                    "Illegal Competition State: expected $from, but got $currentStage"
                )
            }
        }
    }
}

suspend fun CompetitionChangeStageRule.changeStage(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.ChangeStage,
): List<CompGameMessage<CompetitionMessage>> {
    if (player !is InternalPlayer) {
        error("Player $player must be internal")
    }
    return process(player, command)
}

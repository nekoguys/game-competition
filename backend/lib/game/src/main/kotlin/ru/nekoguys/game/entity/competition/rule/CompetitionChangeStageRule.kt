package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.competitionProcessError
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.model.InternalPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load

data class CompetitionStageChangedMessage(
    val from: CompetitionStage,
    val to: CompetitionStage,
) : CompetitionMessage()

@Component
class CompetitionChangeStageRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
) : CompetitionRule<InternalPlayer, CompetitionCommand.ChangeStageCommand, CompetitionMessage> {

    override suspend fun process(
        player: InternalPlayer,
        command: CompetitionCommand.ChangeStageCommand,
    ): List<CompGameMessage<CompetitionMessage>> {
        val session = competitionSessionRepository
            .load(
                player.sessionId,
                CompetitionSession.WithStage,
                CompetitionSession.WithTeamIds,
            )
        checkCurrentStage(session, command)

        competitionSessionRepository.update(
            from = session,
            to = session.copy(
                stage = command.to
            )
        )

        return buildResponse {
            (session.teamIds) {
                CompetitionStageChangedMessage(
                    from = command.from,
                    to = command.to,
                )
            }
        }
    }

    private fun checkCurrentStage(
        session: CompetitionSession.WithStage,
        command: CompetitionCommand.ChangeStageCommand
    ) {
        val currentStage = session.stage

        if (session.stage != command.from) {
            with(command) {
                competitionProcessError(
                    "Illegal Competition State: expected $from, but got $currentStage"
                )
            }
        }
    }
}

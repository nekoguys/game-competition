package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.core.util.defer
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load
import ru.nekoguys.game.entity.competition.service.processError

@Component
class CompetitionStartRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
) : CompetitionRule<CompetitionPlayer.Teacher, CompetitionCommand.Start, Nothing> {

    override suspend fun process(
        player: CompetitionPlayer.Teacher,
        command: CompetitionCommand.Start
    ): List<GameMessage<CompetitionTeam.Id, Nothing>> {
        val currentStage = competitionSessionRepository
            .load(player.sessionId, CompetitionSession.WithStage)
            .stage

        if (currentStage != CompetitionStage.Registration) {
            processError("Game must be in 'Registration' stage")
        }

        return buildResponse {
            defer(
                fromPlayer = InternalPlayer(player.sessionId),
                command = CompetitionCommand.ChangeStage(
                    from = CompetitionStage.Registration,
                    to = CompetitionStage.WaitingStart(round = 1),
                )
            )
        }
    }
}

suspend fun CompetitionStartRule.start(
    player: CompetitionBasePlayer,
): List<GameMessage<CompetitionTeam.Id, Nothing>> {
    if (player !is CompetitionPlayer.Teacher) {
        processError("Player $player must be teacher")
    }
    return process(player, CompetitionCommand.Start)
}

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
class CompetitionStartRoundRule(
    private val competitionSessionRepository: CompetitionSessionRepository,
) : CompetitionRule<CompetitionPlayer.Teacher, CompetitionCommand.StartRound, Nothing> {

    override suspend fun process(
        player: CompetitionPlayer.Teacher,
        command: CompetitionCommand.StartRound
    ): List<GameMessage<CompetitionTeam.Id, Nothing>> {
        val session = competitionSessionRepository
            .load(player.sessionId, CompetitionSession.WithStage)
        val stage = session.stage

        if (stage !is CompetitionStage.WaitingStart) {
            processError("Can't start round, when session is in stage ${stage.name}")
        }

        return buildResponse {
            defer(
                fromPlayer = InternalPlayer(sessionId = player.sessionId),
                command = CompetitionCommand.ChangeStage(
                    from = session.stage,
                    to = CompetitionStage.InProcess(stage.round)
                )
            )
        }
    }
}

suspend fun CompetitionStartRoundRule.startRound(
    player: CompetitionBasePlayer,
): List<GameMessage<CompetitionTeam.Id, Nothing>> {
    if (player !is CompetitionPlayer.Teacher) {
        processError("Player $player must be teacher")
    }
    return process(player, CompetitionCommand.StartRound)
}

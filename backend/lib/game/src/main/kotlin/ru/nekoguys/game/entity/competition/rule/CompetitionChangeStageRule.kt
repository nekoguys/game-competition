package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.CloseGameSessionRequest
import ru.nekoguys.game.core.ResourceLocks
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.core.util.defer
import ru.nekoguys.game.entity.competition.model.InternalPlayer

@Component
class CompetitionChangeStageRule :
    CompetitionRule<InternalPlayer, CompetitionCommand.ChangeStageCommand, CompetitionMessage> {

    override suspend fun getLocksFor(
        command: CompetitionCommand.ChangeStageCommand,
    ): ResourceLocks {
        return super.getLocksFor(command)
    }

    override suspend fun process(
        player: InternalPlayer,
        command: CompetitionCommand.ChangeStageCommand,
    ): List<CompGameMessage<CompetitionMessage>> =
        buildResponse {
            defer(CloseGameSessionRequest, timeoutMillis = 50_000)
        }
}

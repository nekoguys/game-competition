package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionRound

interface CompetitionRoundRepository {
    suspend fun find(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ): CompetitionRound?

    suspend fun startRound(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    )

    suspend fun endRound(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    )

    suspend fun update(
        round: CompetitionRound,
    )
}

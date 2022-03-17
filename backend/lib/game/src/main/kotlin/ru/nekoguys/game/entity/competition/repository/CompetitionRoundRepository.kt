package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonSession

interface CompetitionRoundRepository {
    suspend fun startRound(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    )

    suspend fun endRound(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    )
}

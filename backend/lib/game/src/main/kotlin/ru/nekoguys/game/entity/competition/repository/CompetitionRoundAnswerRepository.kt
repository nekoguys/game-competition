package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

interface CompetitionRoundAnswerRepository {
    suspend fun save(
        answer: CompetitionRoundAnswer,
    )

    suspend fun find(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
        roundNumber: Int,
    ): CompetitionRoundAnswer?

    fun findAll(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionRoundAnswer>

    fun findAll(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    ): Flow<CompetitionRoundAnswer>

    suspend fun delete(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
        roundNumber: Int,
    )
}

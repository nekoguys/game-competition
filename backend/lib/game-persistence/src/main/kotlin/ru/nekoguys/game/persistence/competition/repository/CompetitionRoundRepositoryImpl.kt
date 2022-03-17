package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundRepository

@Repository
class CompetitionRoundRepositoryImpl(
    private val databaseClient: DatabaseClient,
) : CompetitionRoundRepository {

    override suspend fun startRound(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ) {
        insertDbRound(sessionId, roundNumber)
    }

    private suspend fun insertDbRound(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ) {
        databaseClient
            .sql(
                """
                    INSERT INTO competition_round_infos (session_id, round_number, start_time)
                    VALUES (:sessionId, :roundNumber, NOW())
                    """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .bind("roundNumber", roundNumber)
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun endRound(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ) {
        updateRoundEndTime(sessionId, roundNumber)
    }

    private suspend fun updateRoundEndTime(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ) {
        databaseClient
            .sql(
                """
                    UPDATE competition_round_infos
                    SET end_time = NOW()
                    WHERE session_id = :sessionId AND round_number = :roundNumber
                    """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .bind("roundNumber", roundNumber)
            .then()
            .awaitSingleOrNull()
    }
}

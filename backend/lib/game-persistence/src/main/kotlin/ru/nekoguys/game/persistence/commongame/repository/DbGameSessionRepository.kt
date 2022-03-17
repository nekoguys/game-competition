package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.commongame.model.DbGameSession

interface DbGameSessionRepository : CoroutineCrudRepository<DbGameSession, Long> {
    @Query(
        """
        SELECT s.id
        FROM game_sessions AS s
        WHERE s.creator_id = :creatorId
        ORDER BY s.last_modified_date DESC
        LIMIT :limit OFFSET :offset
    """
    )
    fun findIdsByCreatorId(
        creatorId: Long,
        limit: Int,
        offset: Int,
    ): Flow<Long>

    @Query(
        """
        SELECT DISTINCT gs.*
        FROM game_sessions AS gs
        LEFT JOIN competition_teams as ct ON ct.session_id = gs.id
        LEFT JOIN competition_team_members AS ctm ON ctm.team_id = ct.id
        WHERE gs.creator_id = :user_id 
           OR ctm.user_id = :user_id
        ORDER BY gs.last_modified_date DESC
        LIMIT :limit
        OFFSET :offset
     """
    )
    fun findByParticipantId(
        participantId: Long,
        limit: Int,
        offset: Int,
    ): Flow<DbGameSession>

    @Suppress(
        "SpringDataRepositoryMethodParametersInspection",
        "SpringDataRepositoryMethodReturnTypeInspection",
    )
    suspend fun existsByIdAndCreatorId(
        id: Long,
        creatorId: Long,
    ): Boolean
}

fun DbGameSessionRepository.findIdsByParticipantId(
    participantId: Long,
    limit: Int,
    offset: Int,
): Flow<Long> =
    findByParticipantId(
        participantId = participantId,
        limit = limit,
        offset = offset,
    ).map { it.id!! }

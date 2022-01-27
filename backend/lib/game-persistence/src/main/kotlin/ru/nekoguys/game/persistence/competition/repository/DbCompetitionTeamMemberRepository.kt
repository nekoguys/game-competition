package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamMember

interface DbCompetitionTeamMemberRepository : CoroutineCrudRepository<DbCompetitionTeamMember, Long> {
    suspend fun findAllByTeamId(teamId: Long): Flow<DbCompetitionTeamMember>
}

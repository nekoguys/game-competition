package ru.selemilka.game.teams.competition.postgres.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.selemilka.game.teams.competition.postgres.model.DbCompetitionTeamMember

@Repository
interface DbCompetitionTeamMemberRepository : CoroutineCrudRepository<DbCompetitionTeamMember, Long> {
    suspend fun findAllByTeamId(teamId: Long): Flow<DbCompetitionTeamMember>
}
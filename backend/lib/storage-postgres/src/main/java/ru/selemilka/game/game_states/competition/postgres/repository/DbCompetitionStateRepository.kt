package ru.selemilka.game.game_states.competition.postgres.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.game_states.competition.postgres.model.DbCompetitionState

interface DbCompetitionStateRepository : CoroutineCrudRepository<DbCompetitionState, Long> {
    suspend fun findFirstByState(state: DbCompetitionState.State) : DbCompetitionState?
}
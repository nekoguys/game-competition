package ru.selemilka.game.game_states.competition.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.selemilka.game.game_states.competition.model.DbCompetitionState

interface DbCompetitionStateRepository : CoroutineCrudRepository<DbCompetitionState, Long> {
    suspend fun findFirstByState(state: DbCompetitionState.State) : DbCompetitionState?
}
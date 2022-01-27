package ru.nekoguys.game.persistence.competition.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionState

interface DbCompetitionStateRepository : CoroutineCrudRepository<DbCompetitionState, Long> {
    suspend fun findFirstByState(state: DbCompetitionState.State) : DbCompetitionState?
}

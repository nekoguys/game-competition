package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProperties

interface DbCompetitionPropertiesRepository
    : CoroutineSortingRepository<DbCompetitionProperties, Long> {

    fun findAllByIdIn(
        ids: Collection<Long>,
        sort: Sort = Sort.by(DbCompetitionProperties::sessionId.name),
    ): Flow<DbCompetitionProperties>
}

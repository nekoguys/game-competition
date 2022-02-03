package ru.nekoguys.game.persistence.commongame.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties

interface DbGamePropertiesRepository : CoroutineCrudRepository<DbGameProperties, Long> {
    fun findAllByCreatorId(creatorId: Long, page: Pageable): Flow<DbGameProperties>
}

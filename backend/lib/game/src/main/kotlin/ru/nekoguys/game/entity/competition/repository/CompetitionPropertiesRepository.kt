package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.user.model.User

interface CompetitionPropertiesRepository {
    suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
    ): CompetitionProperties

    suspend fun load(
        propertiesId: CommonProperties.Id,
    ): CompetitionProperties

    suspend fun loadBySessionId(
        sessionId: CommonSession.Id,
    ): CompetitionProperties

    suspend fun find(
        propertiesId: Long,
    ): CompetitionProperties?

    fun findAll(
        propertiesIds: Collection<Long>,
    ): Flow<CompetitionProperties>

    fun findAllByCreatorId(
        creatorId: Long,
        page: Pageable,
    ): Flow<CompetitionProperties>
}

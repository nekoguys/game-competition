package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.user.model.User

interface CompetitionSessionRepository {
    suspend fun create(
        propertiesId: CommonProperties.Id,
        stage: CompetitionStage,
    ): CompetitionSession

    suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
        stage: CompetitionStage,
    ): CompetitionSession

    suspend fun findSessionId(id: Long): CommonSession.Id?

    suspend fun load(id: CommonSession.Id): CompetitionSession =
        find(id.number) ?: error("Session with id $id must exist")

    suspend fun find(id: Long): CompetitionSession?

    suspend fun findByCreatorId(
        creatorId: Long,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0,
    ): List<CompetitionSession>
}

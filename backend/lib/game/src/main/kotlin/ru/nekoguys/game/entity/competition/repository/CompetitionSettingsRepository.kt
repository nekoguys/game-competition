package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSettings

interface CompetitionSettingsRepository {
    suspend fun save(
        sessionId: CommonSession.Id,
        settings: CompetitionSettings,
    ): CompetitionSettings

    suspend fun update(
        sessionId: CommonSession.Id,
        settings: CompetitionSettings,
    )

    suspend fun load(
        sessionId: CommonSession.Id,
    ): CompetitionSettings

    suspend fun findAll(
        sessionIds: Collection<Long>,
    ): Map<CommonSession.Id, CompetitionSettings>
}

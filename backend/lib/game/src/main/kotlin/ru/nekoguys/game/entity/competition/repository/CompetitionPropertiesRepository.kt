package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonProperties
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

    suspend fun find(
        propertiesId: Long,
    ): CompetitionProperties?
}

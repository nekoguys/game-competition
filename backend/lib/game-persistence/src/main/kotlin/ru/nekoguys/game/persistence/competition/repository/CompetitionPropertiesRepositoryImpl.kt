package ru.nekoguys.game.persistence.competition.repository

import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.competition.model.toCompetitionSettings
import ru.nekoguys.game.persistence.competition.model.toDbCompetitionProperties

@Repository
class CompetitionPropertiesRepositoryImpl(
    private val dbGamePropertiesRepository: DbGamePropertiesRepository,
    private val dbCompetitionPropertiesRepository: DbCompetitionPropertiesRepository,
) : CompetitionPropertiesRepository {

    override suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
    ): CompetitionProperties {
        val dbGameProperties = DbGameProperties(
            id = null,
            creatorId = userId.number,
            gameType = DbGameType.COMPETITION,
        ).let { dbGamePropertiesRepository.save(it) }

        val dbCompetitionGameProperties = settings
            .toDbCompetitionProperties(parentId = dbGameProperties.id)
            .let { dbCompetitionPropertiesRepository.save(it.asNew()) }

        return CompetitionProperties(
            id = CommonProperties.Id(dbGameProperties.id!!),
            creatorId = User.Id(dbGameProperties.creatorId),
            settings = dbCompetitionGameProperties.toCompetitionSettings()
        )
    }

    override suspend fun load(
        propertiesId: CommonProperties.Id,
    ): CompetitionProperties =
        find(propertiesId.number)
            ?: error("There are no competition properties with ID $propertiesId")

    override suspend fun find(
        propertiesId: Long,
    ): CompetitionProperties? {
        val dbGameProperties =
            dbGamePropertiesRepository.findById(propertiesId)
                ?.takeIf { it.gameType == DbGameType.COMPETITION }
                ?: return null

        val dbCompetitionProperties =
            dbCompetitionPropertiesRepository.findById(propertiesId)
                ?: error("Competition game properties doesn't have saved settings: $dbGameProperties")

        return CompetitionProperties(
            id = CommonProperties.Id(propertiesId),
            creatorId = User.Id(dbGameProperties.creatorId),
            settings = dbCompetitionProperties.toCompetitionSettings(),
        )
    }
}

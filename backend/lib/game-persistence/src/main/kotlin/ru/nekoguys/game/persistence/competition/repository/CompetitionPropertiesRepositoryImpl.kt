package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.persistence.commongame.model.DbGameProperties
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionProperties
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

    override suspend fun findAllByIds(
        propertiesIds: Collection<Long>,
    ): List<CompetitionProperties> = coroutineScope {
        val dbGameProperties = async {
            dbGamePropertiesRepository
                .findAllById(propertiesIds)
                .toList()
        }

        val dbCompetitionProperties = async {
            dbCompetitionPropertiesRepository
                .findAllById(propertiesIds)
                .toList()
        }

        createCompetitionProperties(
            dbGameProperties = dbGameProperties.await(),
            dbCompetitionProperties = dbCompetitionProperties.await(),
        )
    }

    private fun createCompetitionProperties(
        dbGameProperties: List<DbGameProperties>,
        dbCompetitionProperties: List<DbCompetitionProperties>,
    ): List<CompetitionProperties> {
        val dbCompetitionPropertiesById =
            dbCompetitionProperties.associateBy { it.id!! }

        return dbGameProperties.map {
            CompetitionProperties(
                id = CommonProperties.Id(it.id!!),
                creatorId = User.Id(it.creatorId),
                settings = dbCompetitionPropertiesById
                    .getOrElse(it.id) { error("") }
                    .toCompetitionSettings()
            )
        }
    }

    override suspend fun findByCreatorId(
        creatorId: Long,
        page: Pageable,
    ): List<CompetitionProperties> {
        val properties = dbGamePropertiesRepository
            .findAllByCreatorId(creatorId, page)
            .toList()
            .associateBy { it.id!! }

        val competitionProperties = dbCompetitionPropertiesRepository
            .findAllById(properties.keys)
            .toList()
            .associateBy { it.id }

        return properties
            .map { (id, props) ->
                CompetitionProperties(
                    id = CommonProperties.Id(id),
                    creatorId = User.Id(props.creatorId),
                    settings = competitionProperties.getValue(id).toCompetitionSettings()
                )
            }
    }
}

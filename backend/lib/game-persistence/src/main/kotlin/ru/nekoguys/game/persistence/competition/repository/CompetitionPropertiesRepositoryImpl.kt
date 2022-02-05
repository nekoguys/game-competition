package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.*
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
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

    override suspend fun loadBySessionId(
        sessionId: CommonSession.Id,
    ): CompetitionProperties {
        val dbGameProperties =
            dbGamePropertiesRepository.findBySessionId(sessionId.number)
                ?.takeIf { it.gameType == DbGameType.COMPETITION }
                ?: error("This is not a competition properties")

        val dbCompetitionProperties =
            dbCompetitionPropertiesRepository.findById(dbGameProperties.id!!)
                ?: error("Competition game properties doesn't have saved settings: $dbGameProperties")

        return createCompetitionProperties(
            dbGameProperties,
            dbCompetitionProperties,
        )
    }

    override suspend fun find(
        propertiesId: Long,
    ): CompetitionProperties? =
        findAll(listOf(propertiesId)).single()

    override fun findAll(
        propertiesIds: Collection<Long>,
    ): Flow<CompetitionProperties> {
        val dbGameProperties = dbGamePropertiesRepository
            .findAllByIdIn(propertiesIds)

        val dbCompetitionProperties = dbCompetitionPropertiesRepository
            .findAllByIdIn(propertiesIds)

        return dbGameProperties.zip(
            dbCompetitionProperties,
            ::createCompetitionProperties,
        )
    }

    override fun findAllByCreatorId(
        creatorId: Long,
        page: Pageable,
    ): Flow<CompetitionProperties> = flow {
        val dbGameProperties = dbGamePropertiesRepository
            .findAllByCreatorId(creatorId, page)
            .toList()
            .associateBy { it.id!! }

        dbCompetitionPropertiesRepository
            .findAllById(dbGameProperties.keys)
            .map {
                createCompetitionProperties(
                    dbGameProperties = dbGameProperties.getValue(it.id!!),
                    dbCompetitionProperties = it,
                )
            }
            .let { emitAll(it) }
    }
}

private fun createCompetitionPropertiesList(
    dbGameProperties: List<DbGameProperties>,
    dbCompetitionProperties: List<DbCompetitionProperties>,
): List<CompetitionProperties> {
    val dbCompetitionPropertiesById =
        dbCompetitionProperties.associateBy { it.id!! }

    return dbGameProperties.map {
        createCompetitionProperties(
            dbGameProperties = it,
            dbCompetitionProperties = dbCompetitionPropertiesById.getValue(it.id!!)
        )
    }
}

private fun createCompetitionProperties(
    dbGameProperties: DbGameProperties,
    dbCompetitionProperties: DbCompetitionProperties,
): CompetitionProperties =
    CompetitionProperties(
        id = CommonProperties.Id(dbGameProperties.id!!),
        creatorId = User.Id(dbGameProperties.creatorId),
        settings = dbCompetitionProperties.toCompetitionSettings()
    )

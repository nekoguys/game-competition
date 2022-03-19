package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.repository.CompetitionSettingsRepository
import ru.nekoguys.game.persistence.competition.model.toCompetitionSettings
import ru.nekoguys.game.persistence.competition.model.toDbCompetitionProperties

@Repository
class CompetitionSettingsRepositoryImpl(
    private val dbCompetitionPropertiesRepository: DbCompetitionPropertiesRepository,
) : CompetitionSettingsRepository {

    override suspend fun save(
        sessionId: CommonSession.Id,
        settings: CompetitionSettings,
    ): CompetitionSettings {
        val dbCompetitionGameProperties = settings
            .toDbCompetitionProperties(sessionId = sessionId.number)
            .let { dbCompetitionPropertiesRepository.save(it.asNew()) }

        return dbCompetitionGameProperties.toCompetitionSettings()
    }

    override suspend fun update(
        sessionId: CommonSession.Id,
        settings: CompetitionSettings,
    ) {
        settings
            .toDbCompetitionProperties(sessionId = sessionId.number)
            .let { dbCompetitionPropertiesRepository.save(it) }
    }

    override suspend fun load(
        sessionId: CommonSession.Id,
    ): CompetitionSettings =
        findAll(listOf(sessionId.number))
            .values
            .singleOrNull()
            ?: error("There are no competition properties with ID $sessionId")

    override suspend fun findAll(
        sessionIds: Collection<Long>,
    ): Map<CommonSession.Id, CompetitionSettings> =
        dbCompetitionPropertiesRepository
            .findAllByIdIn(sessionIds)
            .run {
                buildMap {
                    collect {
                        put(CommonSession.Id(it.id!!), it.toCompetitionSettings())
                    }
                }
            }
}

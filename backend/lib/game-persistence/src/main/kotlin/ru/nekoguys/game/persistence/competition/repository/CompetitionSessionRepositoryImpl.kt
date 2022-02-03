package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.extractCompetitionStage
import ru.nekoguys.game.persistence.competition.model.extractLastRound
import ru.nekoguys.game.persistence.competition.model.toDbCompetitionStage
import java.time.temporal.ChronoUnit

@Repository
class CompetitionSessionRepositoryImpl(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val transactionalOperator: TransactionalOperator,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
) : CompetitionSessionRepository {

    override suspend fun create(
        propertiesId: CommonProperties.Id,
        stage: CompetitionStage,
    ): CompetitionSession {
        return transactionalOperator.executeAndAwait {
            val properties = competitionPropertiesRepository.load(propertiesId)
            createInTransaction(properties, stage)
        } ?: error("Can't create a session with propertiesId = $propertiesId, " +
                "stage = $stage")
    }

    override suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
        stage: CompetitionStage,
    ): CompetitionSession {
        return transactionalOperator.executeAndAwait {
            val properties = competitionPropertiesRepository.create(userId, settings)
            createInTransaction(properties, stage)
        } ?: error("Can't create a session with userId = $userId, " +
                "settings = $settings, stage = $stage")
    }

    private suspend fun createInTransaction(
        properties: CompetitionProperties,
        stage: CompetitionStage,
    ): CompetitionSession {
        val dbGameSession = DbGameSession(
            id = null,
            propertiesId = properties.id.number,
        ).let { dbGameSessionRepository.save(it) }

        val dbCompetitionSession = DbCompetitionSession(
            parentId = dbGameSession.id!!,
            stage = stage.toDbCompetitionStage(),
            lastRound = stage.extractLastRound(),
        ).let { dbCompetitionSessionRepository.save(it.asNew()) }

        return createCompetitionSession(
            properties = properties,
            dbGameSession = dbGameSession,
            dbCompetitionSession = dbCompetitionSession,
        )
    }

    override suspend fun load(id: CommonSession.Id): CompetitionSession =
        find(id.number)
            ?: error("")

    suspend fun find(id: Long): CompetitionSession? {
        val dbGameSession =
            dbGameSessionRepository.findById(id)
                ?: return null

        val properties =
            competitionPropertiesRepository.find(dbGameSession.propertiesId)
                ?: error("Competition game session doesn't have saved properties: $dbGameSession")

        val dbCompetitionSession =
            dbCompetitionSessionRepository.findById(id)
                ?: error("Competition game session doesn't have saved state: $dbGameSession")

        return createCompetitionSession(
            properties = properties,
            dbGameSession = dbGameSession,
            dbCompetitionSession = dbCompetitionSession,
        )
    }

    override suspend fun findByCreatorId(
        creatorId: Long,
        limit: Int,
        offset: Int,
    ): List<CompetitionSession> {
        val dbGameSessions = dbGameSessionRepository
            .findAllByCreatorId(creatorId, limit, offset)
            .toList()

        return coroutineScope {
            val propertiesList = async {
                competitionPropertiesRepository
                    .findAllByIds(dbGameSessions.map { it.propertiesId })
            }

            val dbCompetitionSessions = async {
                dbCompetitionSessionRepository
                    .findAllById(dbGameSessions.map { it.id!! })
                    .toList()
            }

            createCompetitionSessions(
                dbGameSessions = dbGameSessions,
                propertiesList = propertiesList.await(),
                dbCompetitionSessions = dbCompetitionSessions.await(),
            )
        }
    }
}

private fun createCompetitionSessions(
    propertiesList: List<CompetitionProperties>,
    dbGameSessions: List<DbGameSession>,
    dbCompetitionSessions: List<DbCompetitionSession>,
): List<CompetitionSession> {
    val propertiesById = propertiesList.associateBy { it.id.number }
    val dbCompetitionSessionsById = dbCompetitionSessions.associateBy { it.id!! }

    return dbGameSessions.map {
        createCompetitionSession(
            properties = propertiesById.getValue(it.propertiesId),
            dbGameSession = it,
            dbCompetitionSession = dbCompetitionSessionsById.getValue(it.id!!),
        )
    }
}

private fun createCompetitionSession(
    properties: CompetitionProperties,
    dbGameSession: DbGameSession,
    dbCompetitionSession: DbCompetitionSession,
): CompetitionSession =
    CompetitionSession(
        id = CommonSession.Id(dbGameSession.id!!),
        properties = properties,
        stage = dbCompetitionSession.extractCompetitionStage(),
        lastModified = dbGameSession
            .lastModifiedDate!!
            .truncatedTo(ChronoUnit.MILLIS)
    )

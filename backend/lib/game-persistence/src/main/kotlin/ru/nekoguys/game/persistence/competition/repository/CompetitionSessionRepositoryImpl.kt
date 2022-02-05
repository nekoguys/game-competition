package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
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
    private val competitionTeamRepository: CompetitionTeamRepository,
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
            dbGameSession = dbGameSession,
            dbCompetitionSession = dbCompetitionSession,
            properties = properties,
            teams = emptyList(),
        )
    }

    override suspend fun findSessionId(id: Long): CommonSession.Id? =
        CommonSession.Id(id)
            .takeIf { dbCompetitionSessionRepository.existsById(id) }

    override suspend fun load(id: CommonSession.Id): CompetitionSession =
        find(id.number)
            ?: error("")

    suspend fun find(id: Long): CompetitionSession? {
        val dbGameSession =
            dbGameSessionRepository.findById(id)
                ?: return null

        return coroutineScope {
            val properties = async {
                competitionPropertiesRepository.find(dbGameSession.propertiesId)
                    ?: error("Competition game session doesn't have saved properties: $dbGameSession")
            }

            val dbCompetitionSession = async {
                dbCompetitionSessionRepository.findById(id)
                    ?: error("Competition game session doesn't have saved state: $dbGameSession")
            }

            val teams = async {
                competitionTeamRepository
                    .loadBySession(CommonSession.Id(dbGameSession.id!!))
                    .toList()
            }

            createCompetitionSession(
                dbGameSession = dbGameSession,
                dbCompetitionSession = dbCompetitionSession.await(),
                properties = properties.await(),
                teams = teams.await(),
            )
        }
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
                    .findAll(dbGameSessions.map { it.propertiesId })
                    .toList()
            }

            val dbCompetitionSessions = async {
                dbCompetitionSessionRepository
                    .findAllById(dbGameSessions.map { it.id!! })
                    .toList()
            }

            val teams = async {
                competitionTeamRepository
                    .loadAllBySession(dbGameSessions
                        .map { CommonSession.Id(it.id!!) })
                    .toList()
            }

            createCompetitionSessions(
                propertiesList = propertiesList.await(),
                dbGameSessions = dbGameSessions,
                dbCompetitionSessions = dbCompetitionSessions.await(),
                teams = teams.await(),
            )
        }
    }
}

private fun createCompetitionSessions(
    propertiesList: List<CompetitionProperties>,
    dbGameSessions: List<DbGameSession>,
    dbCompetitionSessions: List<DbCompetitionSession>,
    teams: List<CompetitionTeam>,
): List<CompetitionSession> {
    val propertiesById = propertiesList.associateBy { it.id.number }
    val dbCompetitionSessionsById = dbCompetitionSessions.associateBy { it.id!! }
    val teamsBySessionId = teams.groupBy { it.sessionId.number }

    return dbGameSessions.map {
        createCompetitionSession(
            dbGameSession = it,
            dbCompetitionSession = dbCompetitionSessionsById.getValue(it.id!!),
            properties = propertiesById.getValue(it.propertiesId),
            teams = teamsBySessionId[it.id!!].orEmpty(),
        )
    }
}

private fun createCompetitionSession(
    dbGameSession: DbGameSession,
    dbCompetitionSession: DbCompetitionSession,
    properties: CompetitionProperties,
    teams: List<CompetitionTeam>,
): CompetitionSession =
    CompetitionSession(
        id = CommonSession.Id(dbGameSession.id!!),
        properties = properties,
        lastModified = dbGameSession
            .lastModifiedDate!!
            .truncatedTo(ChronoUnit.MILLIS),
        stage = dbCompetitionSession.extractCompetitionStage(),
        teams = teams,
    )

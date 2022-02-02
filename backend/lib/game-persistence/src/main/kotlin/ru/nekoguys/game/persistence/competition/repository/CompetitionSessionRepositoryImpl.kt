package ru.nekoguys.game.persistence.competition.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonProperties
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionProperties
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.repository.DbGamePropertiesRepository
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Repository
class CompetitionSessionRepositoryImpl(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    private val transactionalOperator: TransactionalOperator,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbGamePropertiesRepository: DbGamePropertiesRepository,
    private val dbCompetitionPropertiesRepository: DbCompetitionPropertiesRepository,
    val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
) : CompetitionSessionRepository {

    override suspend fun create(
        propertiesId: CommonProperties.Id,
        stage: CompetitionStage,
    ): CompetitionSession {
        return transactionalOperator.executeAndAwait { tx ->
            val properties = competitionPropertiesRepository.load(propertiesId)
            createInTransaction(properties, stage)
        } ?: error("")
    }

    override suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
        stage: CompetitionStage,
    ): CompetitionSession {
        val properties = competitionPropertiesRepository.create(userId, settings)
        return createInTransaction(properties, stage)
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

        return CompetitionSession(
            id = CommonSession.Id(dbGameSession.id!!),
            properties = properties,
            stage = dbCompetitionSession.extractCompetitionStage(),
            lastModified = dbGameSession.lastModifiedDate!!
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

        return CompetitionSession(
            id = CommonSession.Id(dbGameSession.id!!),
            properties = properties,
            stage = dbCompetitionSession.extractCompetitionStage(),
            lastModified = dbGameSession.lastModifiedDate!!
        )
    }
}

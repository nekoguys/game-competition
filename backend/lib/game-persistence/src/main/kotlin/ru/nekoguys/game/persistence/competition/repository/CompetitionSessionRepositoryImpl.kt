package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.*
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSettingsRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.persistence.commongame.model.DbGameSession
import ru.nekoguys.game.persistence.commongame.model.DbGameType
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.extractCompetitionStage
import ru.nekoguys.game.persistence.competition.model.extractLastRound
import ru.nekoguys.game.persistence.competition.model.toDbCompetitionStage
import java.time.temporal.ChronoUnit

@Repository
class CompetitionSessionRepositoryImpl(
    private val competitionSettingsRepository: CompetitionSettingsRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val transactionalOperator: TransactionalOperator,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbCompetitionSessionRepository: DbCompetitionSessionRepository,
) : CompetitionSessionRepository {

    override suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
        stage: CompetitionStage,
    ): CompetitionSession = transactionalOperator.executeAndAwait {
        val dbGameSession = DbGameSession(
            id = null,
            creatorId = userId.number,
            gameType = DbGameType.COMPETITION,
        ).let { dbGameSessionRepository.save(it) }

        val dbCompetitionSession = DbCompetitionSession(
            sessionId = dbGameSession.id!!,
            stage = stage.toDbCompetitionStage(),
            lastRound = stage.extractLastRound(),
        ).let { dbCompetitionSessionRepository.save(it.asNew()) }

        val sessionId = CommonSession.Id(dbGameSession.id!!)
        val savedSettings = competitionSettingsRepository.save(sessionId, settings)

        createCompetitionSession(
            dbCompetitionSession = dbCompetitionSession,
            dbGameSession = dbGameSession,
            settings = savedSettings,
        )
    } ?: error("Can't create a session with userId = $userId, " +
            "settings = $settings, stage = $stage")

    override suspend fun findAll(
        ids: Collection<Long>,
        fieldSelectors: Set<CompetitionSessionFieldSelector<*>>,
    ): List<CompetitionSession> = coroutineScope {
        val dbGameSessions = async {
            dbGameSessionRepository
                .takeIf {
                    CompetitionSession.WithCommonFields in fieldSelectors ||
                            CompetitionSession.Full in fieldSelectors
                }
                ?.findAllById(ids)
                ?.toList()
                ?.associateBy { it.id!! }
        }

        val settings = async {
            competitionSettingsRepository
                .takeIf {
                    CompetitionSession.WithSettings in fieldSelectors ||
                            CompetitionSession.Full in fieldSelectors
                }
                ?.findAll(ids)
                ?.mapKeys { (a) -> a.number }
        }

        val teams = async {
            competitionTeamRepository
                .takeIf {
                    CompetitionSession.WithTeams in fieldSelectors ||
                            CompetitionSession.Full in fieldSelectors
                }
                ?.findAllBySessionIds(ids)
                ?.mapKeys { (a) -> a.number }
        }

        val dbCompetitionSessions = dbCompetitionSessionRepository
            .findAllById(ids)
            .toList()
            .associateBy { it.id!! }

        dbCompetitionSessions.map { (id, dbCompetitionSession) ->
            createCompetitionSession(
                dbCompetitionSession = dbCompetitionSession,
                dbGameSession = dbGameSessions.await()?.getValue(id),
                settings = settings.await()?.getValue(id),
                teams = teams.await()?.run { get(id).orEmpty() },
            )
        }
    }

    override suspend fun update(
        from: CompetitionSession,
        to: CompetitionSession,
    ) = coroutineScope {
        // других типов CompetitionSession и не бывает
        from as CompetitionSession.Full
        to as CompetitionSession.Full

        if (to._settings != null && to.settings != from._settings) {
            launch {
                competitionSettingsRepository.save(to.id, to.settings)
            }
        }

        if (to._stage != null && to.stage != from._stage) {
            DbCompetitionSession(
                sessionId = to.id.number,
                stage = to.stage.toDbCompetitionStage(),
                lastRound = to.stage.extractLastRound(),
            ).let { dbCompetitionSessionRepository.save(it.asNew()) }
        }
    }

    override suspend fun findIdsByCreatorId(
        creatorId: Long,
        limit: Int,
        offset: Int,
    ): List<CommonSession.Id> =
        dbGameSessionRepository
            .findIdsByCreatorId(creatorId, limit, offset)
            .toList()
            .map(CommonSession::Id)
}

private fun createCompetitionSession(
    dbCompetitionSession: DbCompetitionSession,
    dbGameSession: DbGameSession? = null,
    settings: CompetitionSettings? = null,
    teams: List<CompetitionTeam>? = null,
): CompetitionSession.Full =
    CompetitionSession.Full(
        _id = CommonSession.Id(dbCompetitionSession.id!!),
        _settings = settings,
        _creatorId = dbGameSession?.creatorId?.let(User::Id),
        _lastModified = dbGameSession
            ?.lastModifiedDate
            ?.truncatedTo(ChronoUnit.MILLIS),
        _stage = dbCompetitionSession.extractCompetitionStage(),
        _teams = teams,
    )

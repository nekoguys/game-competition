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
import ru.nekoguys.game.persistence.commongame.repository.findIdsByParticipantId
import ru.nekoguys.game.persistence.competition.model.DbCompetitionSession
import ru.nekoguys.game.persistence.competition.model.extractCompetitionStage
import ru.nekoguys.game.persistence.competition.model.extractDbCompetitionStage
import ru.nekoguys.game.persistence.competition.model.extractDbLastRound
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
            stage = stage.extractDbCompetitionStage(),
            lastRound = stage.extractDbLastRound(),
        ).let { dbCompetitionSessionRepository.save(it.asNew()) }

        val sessionId = CommonSession.Id(dbGameSession.id!!)
        val savedSettings = competitionSettingsRepository.save(sessionId, settings)

        createCompetitionSession(
            dbCompetitionSession = dbCompetitionSession,
            dbGameSession = dbGameSession,
            settings = savedSettings,
        )
    } ?: error(
        "Can't create a session with userId = $userId, " +
                "settings = $settings, stage = $stage"
    )

    override suspend fun findAll(
        ids: Collection<Long>,
        fieldSelectors: Set<CompetitionSessionFieldSelector<*>>,
    ): List<CompetitionSession> = coroutineScope {
        val dbGameSessions = async {
            dbGameSessionRepository
                .takeIf { fieldSelectors.has(CompetitionSession.WithCommonFields) }
                ?.findAllById(ids)
                ?.toList()
                ?.associateBy { it.id!! }
        }

        val settings = async {
            competitionSettingsRepository
                .takeIf { fieldSelectors.has(CompetitionSession.WithSettings) }
                ?.findAll(ids)
                ?.mapKeys { (a) -> a.number }
        }

        val teams = async {
            competitionTeamRepository
                .takeIf { fieldSelectors.has(CompetitionSession.WithTeams) }
                ?.findAllBySessionIds(ids)
                ?.mapKeys { (a) -> a.number }
        }

        val teamIds = async {
            competitionTeamRepository
                .takeIf { fieldSelectors.has(CompetitionSession.WithTeamIds) }
                ?.findAllTeamIdsBySessionIds(ids)
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
                teamIds = teamIds.await()?.run { get(id).orEmpty() },
            )
        }
    }

    private fun Set<CompetitionSessionFieldSelector<*>>.has(
        selector: CompetitionSessionFieldSelector<*>,
    ): Boolean =
        contains(CompetitionSession.Full) || contains(selector)

    override suspend fun update(
        from: CompetitionSession,
        to: CompetitionSession,
    ) = coroutineScope {
        // других типов CompetitionSession и не бывает
        from as CompetitionSessionImpl
        to as CompetitionSessionImpl

        val newSettings = to.settingsOrNull
        if (newSettings != null && newSettings != from.settingsOrNull) {
            launch {
                competitionSettingsRepository.save(to.id, newSettings)
            }
        }

        val newStage = to.stageOrNull
        if (newStage != null && newStage != from.stageOrNull) {
            DbCompetitionSession(
                sessionId = to.id.number,
                stage = newStage.extractDbCompetitionStage(),
                lastRound = newStage.extractDbLastRound(),
            ).let { dbCompetitionSessionRepository.save(it) }
        }

        val oldTeams = from.teamsOrNull.orEmpty().toSet()
        val newTeams = to.teamsOrNull.orEmpty().filter { it !in oldTeams }
        if (newTeams.isNotEmpty()) {
            launch {
                TODO("Обновление команд ещё не готово")
            }
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

    override suspend fun findIdsByParticipantId(
        participantId: Long,
        limit: Int,
        offset: Int,
    ): List<CommonSession.Id> =
        dbGameSessionRepository
            .findIdsByParticipantId(
                participantId = participantId,
                limit = limit,
                offset = offset,
            )
            .toList()
            .map(CommonSession::Id)

}

private fun createCompetitionSession(
    dbCompetitionSession: DbCompetitionSession,
    dbGameSession: DbGameSession? = null,
    settings: CompetitionSettings? = null,
    teams: List<CompetitionTeam>? = null,
    teamIds: List<CompetitionTeam.Id>? = null,
): CompetitionSession =
    CompetitionSession(
        id = CommonSession.Id(dbCompetitionSession.id!!),
        settings = settings,
        creatorId = dbGameSession?.creatorId?.let(User::Id),
        lastModified = dbGameSession
            ?.lastModifiedDate
            ?.truncatedTo(ChronoUnit.MILLIS),
        stage = dbCompetitionSession.extractCompetitionStage(),
        teams = teams,
        teamIds = teamIds,
    )

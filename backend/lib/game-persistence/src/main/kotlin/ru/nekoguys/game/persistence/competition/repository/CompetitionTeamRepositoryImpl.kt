package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeam

@Repository
class CompetitionTeamRepositoryImpl(
    private val competitionPlayerRepository: CompetitionPlayerRepository,
    private val dbCompetitionTeamRepository: DbCompetitionTeamRepository,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val transactionalOperator: TransactionalOperator,
) : CompetitionTeamRepository {

    override suspend fun create(
        creator: CompetitionPlayer.Unknown,
        name: String,
        password: String,
    ): CompetitionTeam = transactionalOperator.executeAndAwait {
        val dbTeam = DbCompetitionTeam(
            id = null,
            sessionId = creator.sessionId.number,
            teamNumber = -1, // номер пересчитается после создания кампании
            name = name,
            password = password,
            banRound = null,
            strategy = null,
        )
            .let { dbCompetitionTeamRepository.save(it) }
            .let { generateAndSaveTeamNumber(it) }

        val captain = CompetitionPlayer.TeamCaptain(
            sessionId = creator.sessionId,
            user = creator.user,
            teamId = CompetitionTeam.Id(dbTeam.id!!),
            banRoundNumber = null,
        ).also { competitionPlayerRepository.save(it) }

        createCompetitionTeam(
            dbTeam = dbTeam,
            captain = captain,
            teamMembers = emptyList(),
        )
    }!!

    private suspend fun generateAndSaveTeamNumber(
        dbTeam: DbCompetitionTeam,
    ): DbCompetitionTeam {
        val teamNumber = dbCompetitionTeamRepository
            .countBySessionIdAndIdLessThanEqual(dbTeam.sessionId, dbTeam.id!!)

        return dbCompetitionTeamRepository.save(dbTeam.copy(teamNumber = teamNumber))
    }

    override suspend fun update(
        teamsDiff: Collection<Pair<CompetitionTeam, CompetitionTeam>>,
    ): Unit = transactionalOperator.executeAndAwait {
        teamsDiff
            .filter { (from, to) ->
                require(from.id == to.id)
                require(from.sessionId == to.sessionId)
                require(from.numberInGame == to.numberInGame)
                require(from.captain == to.captain)

                from.name != to.name
                        || from.captain != to.captain
                        || from.teamMembers != to.teamMembers
                        || from.banRoundNumber != to.banRoundNumber
                        || from.strategy != to.strategy
            }
            .map { (_, team) ->
                DbCompetitionTeam(
                    id = team.id.number,
                    sessionId = team.sessionId.number,
                    teamNumber = team.numberInGame,
                    name = team.name,
                    password = team.password,
                    banRound = team.banRoundNumber,
                    strategy = team.strategy,
                )
            }
            .let { dbCompetitionTeamRepository.saveAll(it) }
            .collect()

        teamsDiff
            .forEach { (from, to) ->
                val newTeamMembers = to.teamMembers - from.teamMembers.toSet()
                for (teamMember in newTeamMembers) {
                    competitionPlayerRepository.save(teamMember)
                }
            }
    }!!

    override fun findAllByIds(
        ids: Collection<Long>,
    ): Flow<CompetitionTeam> =
        dbCompetitionTeamRepository
            .findAllById(ids)
            .map { dbTeam ->
                val sessionId = CommonSession.Id(dbTeam.sessionId)

                val members = competitionPlayerRepository
                    .loadAllInTeam(sessionId, CompetitionTeam.Id(dbTeam.id!!))
                    .toList()

                createCompetitionTeam(
                    dbTeam = dbTeam,
                    captain = members
                        .filterIsInstance<CompetitionPlayer.TeamCaptain>()
                        .single(),
                    teamMembers = members
                        .filterIsInstance<CompetitionPlayer.TeamMember>()
                )
            }

    override suspend fun findByName(
        sessionId: CommonSession.Id,
        teamName: String,
    ): CompetitionTeam? {
        val dbTeam = dbCompetitionTeamRepository
            .findBySessionIdAndName(
                sessionId = sessionId.number,
                name = teamName,
            )
            ?: return null

        val members = competitionPlayerRepository
            .loadAllInTeam(sessionId, CompetitionTeam.Id(dbTeam.id!!))
            .toList()

        return createCompetitionTeam(
            dbTeam = dbTeam,
            captain = members
                .filterIsInstance<CompetitionPlayer.TeamCaptain>()
                .single(),
            teamMembers = members
                .filterIsInstance<CompetitionPlayer.TeamMember>()
        )
    }

    override fun findBySessionId(
        sessionId: Long,
    ): Flow<CompetitionTeam> = flow {
        coroutineScope {
            val dbTeamsDeferred = async {
                dbCompetitionTeamRepository
                    .findAllBySessionId(sessionId)
                    .toList()
            }

            val allMembersDeferred = async {
                competitionPlayerRepository
                    .loadAllInSession(sessionId)
                    .toList()
            }

            val allMembers = allMembersDeferred
                .await()
                .groupBy { it.teamId.number }

            dbTeamsDeferred
                .await()
                .map { dbTeam ->
                    val members = allMembers[dbTeam.id!!].orEmpty()
                    val captain = members
                        .filterIsInstance<CompetitionPlayer.TeamCaptain>()
                        .single()
                    val teamMembers = members
                        .filterIsInstance<CompetitionPlayer.TeamMember>()

                    createCompetitionTeam(
                        dbTeam,
                        captain = captain,
                        teamMembers = teamMembers,
                    )
                }
                .asFlow()
        }.collect { team -> emit(team) }
    }

    @OptIn(FlowPreview::class)
    override suspend fun findAllBySessionIds(
        sessionIds: Iterable<Long>,
    ): Map<CommonSession.Id, List<CompetitionTeam>> =
        sessionIds
            .asFlow()
            .flatMapMerge { sessionId ->
                findBySessionId(sessionId)
                    .map { sessionId to it }
            }
            .toList()
            .groupBy({ CommonSession.Id(it.first) }) { it.second }

    override suspend fun findAllTeamIdsBySessionIds(
        sessionIds: Collection<Long>,
    ): Map<CommonSession.Id, List<CompetitionTeam.Id>> =
        dbCompetitionTeamRepository
            .findAllBySessionIdIn(sessionIds)
            .toList()
            .groupBy({ CommonSession.Id(it.sessionId) }) {
                CompetitionTeam.Id(it.id!!)
            }

    override suspend fun load(
        teamId: CompetitionTeam.Id
    ): CompetitionTeam =
        findAllByIds(listOf(teamId.number))
            .singleOrNull() ?: error("Team with id $teamId must exist")
}

private fun createCompetitionTeam(
    dbTeam: DbCompetitionTeam,
    captain: CompetitionPlayer.TeamCaptain,
    teamMembers: List<CompetitionPlayer.TeamMember>,
): CompetitionTeam =
    CompetitionTeam(
        id = captain.teamId,
        sessionId = CommonSession.Id(dbTeam.sessionId),
        name = dbTeam.name,
        numberInGame = dbTeam.teamNumber,
        captain = captain,
        teamMembers = teamMembers,
        banRoundNumber = dbTeam.banRound,
        password = dbTeam.password,
        strategy = dbTeam.strategy,
    )

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
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val transactionalOperator: TransactionalOperator,
    private val competitionPlayerRepository: CompetitionPlayerRepository,
    private val dbCompetitionTeamRepository: DbCompetitionTeamRepository,
) : CompetitionTeamRepository {

    override suspend fun create(
        creator: CompetitionPlayer.Unknown,
        name: String,
        maxTeams: Int,
    ): CompetitionTeam = transactionalOperator.executeAndAwait {
        val dbTeam = DbCompetitionTeam(
            id = null,
            sessionId = creator.sessionId.number,
            teamNumber = -1,
            name = name,
            banRound = null,
        ).let { dbCompetitionTeamRepository.save(it) }
            .let {
                val teamNumber = dbCompetitionTeamRepository
                    .countBySessionIdAndIdLessThanEqual(it.sessionId, it.id!!)
                if (teamNumber > maxTeams) {
                    error("No more teams are allowed")
                }
                it.copy(teamNumber = teamNumber)
            }
            .let { dbCompetitionTeamRepository.save(it) }

        val captain = CompetitionPlayer.TeamCaptain(
            sessionId = creator.sessionId,
            user = creator.user,
            teamId = CompetitionTeam.Id(dbTeam.id!!)
        ).also { competitionPlayerRepository.save(it) }

        createCompetitionTeam(
            dbTeam = dbTeam,
            captain = captain,
            teamMates = emptyList(),
        )
    }!!

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
            teamMates = members
                .filterIsInstance<CompetitionPlayer.TeamMate>()
        )
    }

    override fun loadBySession(
        sessionId: CommonSession.Id,
    ): Flow<CompetitionTeam> = flow {
        coroutineScope {
            val allMembersDeferred = async {
                competitionPlayerRepository
                    .loadAllInSession(sessionId)
                    .toList()
            }

            val dbTeamsDeferred = async {
                dbCompetitionTeamRepository
                    .findAllBySessionId(sessionId.number)
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
                        .first { it is CompetitionPlayer.TeamCaptain }
                            as CompetitionPlayer.TeamCaptain
                    val teamMates = members
                        .filterIsInstance<CompetitionPlayer.TeamMate>()

                    createCompetitionTeam(
                        dbTeam,
                        captain = captain,
                        teamMates = teamMates,
                    )
                }
                .asFlow()
        }.collect { team -> emit(team) }
    }

    @OptIn(FlowPreview::class)
    override fun loadAllBySession(
        sessionIds: Iterable<CommonSession.Id>,
    ): Flow<CompetitionTeam> =
        sessionIds
            .asFlow()
            .flatMapMerge { loadBySession(it) }
}

private fun createCompetitionTeam(
    dbTeam: DbCompetitionTeam,
    captain: CompetitionPlayer.TeamCaptain,
    teamMates: List<CompetitionPlayer.TeamMate>,
): CompetitionTeam =
    CompetitionTeam(
        id = captain.teamId,
        sessionId = CommonSession.Id(dbTeam.sessionId),
        name = dbTeam.name,
        captain = captain,
        teamMates = teamMates,
        isBanned = dbTeam.banRound != null,
    )

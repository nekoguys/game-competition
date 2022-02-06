package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.persistence.commongame.repository.DbGameSessionRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionTeamMember

@Repository
class CompetitionPlayerRepositoryImpl(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val transactionalOperator: TransactionalOperator,
    private val userRepository: UserRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbCompetitionTeamMemberRepository: DbCompetitionTeamMemberRepository,
) : CompetitionPlayerRepository {

    override suspend fun save(
        player: CompetitionPlayer.TeamMember,
        maxPlayers: Int,
    ) = transactionalOperator.executeAndAwait {
        val dbTeamMember = DbCompetitionTeamMember(
            id = null,
            teamId = player.teamId.number,
            userId = player.user.id.number,
            captain = player is CompetitionPlayer.TeamCaptain,
        ).let { dbCompetitionTeamMemberRepository.save(it) }

        val membersAlreadyRegistered = dbCompetitionTeamMemberRepository
            .countByTeamIdAndIdLessThanEqual(
                teamId = dbTeamMember.teamId,
                id = dbTeamMember.id!!,
            )
        if (membersAlreadyRegistered > maxPlayers) {
            error("")
        }
    }!!

    override suspend fun load(
        sessionId: CommonSession.Id,
        user: User,
    ): CompetitionPlayer {
        val dbCompetitionTeamMember = dbCompetitionTeamMemberRepository
            .findBySessionIdAndUserId(
                sessionId = sessionId.number,
                userId = user.id.number,
            )

        if (dbCompetitionTeamMember != null) {
            return createTeamMember(
                dbCompetitionTeamMember,
                sessionId,
                user,
            )
        }

        val isTeacher = dbGameSessionRepository
            .existsByIdAndCreatorId(
                sessionId = sessionId.number,
                creatorId = user.id.number,
            )
        if (isTeacher) {
            return CompetitionPlayer.Teacher(sessionId, user)
        }

        return CompetitionPlayer.Unknown(sessionId, user)
    }

    override fun loadAllInTeam(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionPlayer.TeamMember> = flow {
        val dbCompetitionTeamMembersByUserId =
            dbCompetitionTeamMemberRepository
                .findAllByTeamId(teamId.number)
                .toList()
                .associateBy { it.userId }

        userRepository
            .findAll(dbCompetitionTeamMembersByUserId.keys)
            .map { user ->
                createTeamMember(
                    dbCompetitionTeamMembersByUserId.getValue(user.id.number),
                    sessionId,
                    user
                )
            }
            .collect { player -> emit(player) }
    }

    override fun loadAllInSession(
        sessionId: CommonSession.Id,
    ): Flow<CompetitionPlayer.TeamMember> = flow {
        val dbCompetitionTeamMembersByUserId =
            dbCompetitionTeamMemberRepository
                .findAllBySessionIds(listOf(sessionId.number))
                .toList()
                .associateBy { it.userId }

        userRepository
            .findAll(dbCompetitionTeamMembersByUserId.keys)
            .map { user ->
                createTeamMember(
                    dbCompetitionTeamMembersByUserId.getValue(user.id.number),
                    sessionId,
                    user
                )
            }
            .collect { player -> emit(player) }
    }
}

private fun createTeamMember(
    dbCompetitionTeamMember: DbCompetitionTeamMember,
    sessionId: CommonSession.Id,
    user: User,
): CompetitionPlayer.TeamMember {
    val teamId = CompetitionTeam.Id(dbCompetitionTeamMember.teamId)
    return if (dbCompetitionTeamMember.captain) {
        CompetitionPlayer.TeamCaptain(sessionId, user, teamId)
    } else {
        CompetitionPlayer.TeamMate(sessionId, user, teamId)
    }
}

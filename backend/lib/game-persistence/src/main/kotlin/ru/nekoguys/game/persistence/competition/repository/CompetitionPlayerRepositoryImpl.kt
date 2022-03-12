package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
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
    private val userRepository: UserRepository,
    private val dbGameSessionRepository: DbGameSessionRepository,
    private val dbCompetitionTeamMemberRepository: DbCompetitionTeamMemberRepository,
) : CompetitionPlayerRepository {

    private val logger = LoggerFactory.getLogger(CompetitionPlayerRepositoryImpl::class.java)

    override suspend fun save(
        player: CompetitionPlayer.Student,
    ) {
        val dbTeamMember = DbCompetitionTeamMember(
            id = null,
            teamId = player.teamId.number,
            userId = player.user.id.number,
            captain = player is CompetitionPlayer.TeamCaptain,
        )

        dbCompetitionTeamMemberRepository
            .save(dbTeamMember)
            .also { logger.info("Saved player $player to DB") }
    }

    override suspend fun load(
        sessionId: CommonSession.Id,
        user: User,
    ): CompetitionPlayer {
        val dbCompetitionTeamMember = dbCompetitionTeamMemberRepository
            .findBySessionIdAndUserId(
                sessionId = sessionId.number,
                userId = user.id.number,
            )

        return when {
            dbCompetitionTeamMember != null -> {
                createTeamMember(
                    dbCompetitionTeamMember,
                    sessionId,
                    user,
                ).also { logger.info("Loaded team member $it from DB") }
            }

            ifUserIsTeacher(sessionId, user) -> {
                CompetitionPlayer.Teacher(sessionId, user)
                    .also { logger.info("Loaded teacher $it from DB") }
            }

            else -> {
                CompetitionPlayer.Unknown(sessionId, user)
                    .also {
                        logger.info("Player with userId=${user.id} is not associated with session $sessionId")
                    }
            }
        }
    }

    private suspend fun ifUserIsTeacher(
        sessionId: CommonSession.Id,
        user: User
    ): Boolean =
        dbGameSessionRepository
            .existsByIdAndCreatorId(
                id = sessionId.number,
                creatorId = user.id.number,
            )

    override fun loadAllInTeam(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionPlayer.Student> = flow {
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
        sessionId: Long,
    ): Flow<CompetitionPlayer.Student> = flow {
        val dbCompetitionTeamMembersByUserId =
            dbCompetitionTeamMemberRepository
                .findAllBySessionIds(listOf(sessionId))
                .toList()
                .associateBy { it.userId }

        userRepository
            .findAll(dbCompetitionTeamMembersByUserId.keys)
            .map { user ->
                createTeamMember(
                    dbCompetitionTeamMembersByUserId.getValue(user.id.number),
                    CommonSession.Id(sessionId),
                    user,
                )
            }
            .collect { player -> emit(player) }
    }
}

private fun createTeamMember(
    dbCompetitionTeamMember: DbCompetitionTeamMember,
    sessionId: CommonSession.Id,
    user: User,
): CompetitionPlayer.Student {
    val teamId = CompetitionTeam.Id(dbCompetitionTeamMember.teamId)
    return if (dbCompetitionTeamMember.captain) {
        CompetitionPlayer.TeamCaptain(sessionId, user, teamId)
    } else {
        CompetitionPlayer.TeamMember(sessionId, user, teamId)
    }
}

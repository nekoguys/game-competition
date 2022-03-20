package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.user.model.User

/**
 * Игрок в Конкуренции
 *
 * Классы-наследники образуют иерархию:
 *
 * [CompetitionBasePlayer]
 * |   [InternalPlayer]
 * |   [CompetitionPlayer]
 * |   |   [CompetitionPlayer.Unknown]
 * |   |   [CompetitionPlayer.Teacher]
 * |   |   [CompetitionPlayer.Student]
 * |   |   |   [CompetitionPlayer.TeamCaptain]
 * |   |   |   [CompetitionPlayer.TeamMember]
 * |   [BannedCompetitionPlayer]
 */
sealed interface CompetitionBasePlayer {
    val sessionId: CommonSession.Id
}

/**
 * Используется для завершения раунда и прочих действий
 */
data class InternalPlayer(
    override val sessionId: CommonSession.Id,
) : CompetitionBasePlayer

sealed interface CompetitionPlayer : CompetitionBasePlayer {
    val user: User

    data class Unknown(
        override val sessionId: CommonSession.Id,
        override val user: User,
    ) : CompetitionPlayer

    data class Teacher(
        override val sessionId: CommonSession.Id,
        override val user: User,
    ) : CompetitionPlayer

    sealed interface Student : CompetitionPlayer {
        val teamId: CompetitionTeam.Id
        val banRoundNumber: Int?
    }

    data class TeamCaptain(
        override val sessionId: CommonSession.Id,
        override val user: User,
        override val teamId: CompetitionTeam.Id,
        override val banRoundNumber: Int?
    ) : Student

    data class TeamMember(
        override val sessionId: CommonSession.Id,
        override val user: User,
        override val teamId: CompetitionTeam.Id,
        override val banRoundNumber: Int?
    ) : Student
}

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
 * |   |   [CompetitionPlayer.TeamMember]
 * |   |   |   [CompetitionPlayer.TeamCaptain]
 * |   |   |   [CompetitionPlayer.TeamMate]
 */
sealed interface CompetitionBasePlayer {
    val sessionId : CommonSession.Id
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

    sealed interface TeamMember : CompetitionPlayer {
        val teamId: CompetitionTeam.Id
    }

    data class TeamCaptain(
        override val sessionId: CommonSession.Id,
        override val user: User,
        override val teamId: CompetitionTeam.Id,
    ) : TeamMember

    data class TeamMate(
        override val sessionId: CommonSession.Id,
        override val user: User,
        override val teamId: CompetitionTeam.Id,
    ) : TeamMember
}




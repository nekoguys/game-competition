package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession
import java.time.LocalDateTime

sealed interface CompetitionRound {
    val sessionId: CommonSession.Id
    val roundNumber: Int
    val answers: List<CompetitionRoundAnswer>
    val startTime: LocalDateTime

    data class Current(
        override val sessionId: CommonSession.Id,
        override val roundNumber: Int,
        override val answers: List<CompetitionRoundAnswer>,
        override val startTime: LocalDateTime,
    ) : CompetitionRound

    data class Ended(
        override val sessionId: CommonSession.Id,
        override val roundNumber: Int,
        override val answers: List<CompetitionRoundAnswer>,
        val result: List<CompetitionRoundResult>,
        override val startTime: LocalDateTime,
        val endTime: LocalDateTime,
    ) : CompetitionRound
}

data class CompetitionRoundAnswer(
    val sessionId: CommonSession.Id,
    val teamId: CompetitionTeam.Id,
    val roundNumber: Long,
    val income: Long,
)

data class CompetitionRoundResult(
    val sessionId: CommonSession.Id,
    val teamId: CompetitionTeam.Id,
    val roundNumber: Long,
    val income: Long,
)

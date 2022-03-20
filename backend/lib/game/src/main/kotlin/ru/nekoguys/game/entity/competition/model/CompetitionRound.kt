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
        override val answers: List<CompetitionRoundAnswer.WithIncome>,
        override val startTime: LocalDateTime,
        val endTime: LocalDateTime,
    ) : CompetitionRound
}

sealed interface CompetitionRoundAnswer {
    val sessionId: CommonSession.Id
    val teamId: CompetitionTeam.Id
    val roundNumber: Int
    val value: Int

    data class Impl(
        override val sessionId: CommonSession.Id,
        override val teamId: CompetitionTeam.Id,
        override val roundNumber: Int,
        override val value: Int,
    ) : CompetitionRoundAnswer

    data class WithIncome(
        override val sessionId: CommonSession.Id,
        override val teamId: CompetitionTeam.Id,
        override val roundNumber: Int,
        override val value: Int,
        val income: Double,
    ) : CompetitionRoundAnswer
}

fun CompetitionRoundAnswer.withoutIncome() =
    if (this is CompetitionRoundAnswer.Impl) {
        this
    } else {
        CompetitionRoundAnswer.Impl(
            sessionId = sessionId,
            teamId = teamId,
            roundNumber = roundNumber,
            value = value,
        )
    }

fun CompetitionRoundAnswer.withIncome(income: Double) =
    if (this is CompetitionRoundAnswer.WithIncome) {
        this.copy(income = income)
    } else {
        CompetitionRoundAnswer.WithIncome(
            sessionId = sessionId,
            teamId = teamId,
            roundNumber = roundNumber,
            value = value,
            income = income,
        )
    }

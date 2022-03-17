package ru.nekoguys.game.persistence.competition.model

import java.time.LocalDateTime

data class DbCompetitionRoundInfo(
    var sessionId: Long,
    var roundNumber: Int,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
) {
    val isEnded: Boolean
        get() = endTime != null
}

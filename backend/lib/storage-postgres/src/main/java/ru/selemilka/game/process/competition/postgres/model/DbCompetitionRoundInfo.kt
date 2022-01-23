package ru.selemilka.game.process.competition.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("competition_round_infos")
data class DbCompetitionRoundInfo(
    @Id
    var id: Long? = null,
    var processId: Long,
    var roundNumber: Long,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime?,
) {
    val isEnded: Boolean
        get() = endTime != null
}

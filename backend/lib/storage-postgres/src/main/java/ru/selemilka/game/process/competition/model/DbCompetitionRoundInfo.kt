package ru.selemilka.game.process.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("competition_round_infos")
data class DbCompetitionRoundInfo(
    @Id
    var id: Long? = null,
    var processId: Long,
    var roundNumber: Long,
    var startTime: Instant,
    var endTime: Instant?,
) {
    val isEnded: Boolean
        get() = endTime != null
}
package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_round_results")
data class DbCompetitionRoundResult(
    @Id
    var id: Long? = null,
    var roundId: Long,
    var teamId: Long,
    var income: Double,
) {
    constructor(roundInfoId: Long, teamId: Long, income: Double) : this(null, roundInfoId, teamId, income)
}

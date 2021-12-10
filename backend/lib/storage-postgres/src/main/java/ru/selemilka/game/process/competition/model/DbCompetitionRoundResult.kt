package ru.selemilka.game.process.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_round_results")
data class DbCompetitionRoundResult(
    @Id
    var id: Long? = null,
    var roundInfoId: Long,
    var teamId: Long,
    var income: Double,
)
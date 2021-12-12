package ru.selemilka.game.process.competition.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_round_answers")
data class DbCompetitionRoundAnswer(
    @Id
    var id: Long? = null,
    var roundInfoId: Long,
    var teamId: Long,
    var value: Int
) {
    constructor(roundInfoId: Long, teamId: Long, value: Int) : this(null, roundInfoId, teamId, value)
}
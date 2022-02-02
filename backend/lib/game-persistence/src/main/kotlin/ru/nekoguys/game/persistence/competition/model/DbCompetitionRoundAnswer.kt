package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_round_answers")
data class DbCompetitionRoundAnswer(
    @Id
    var id: Long? = null,

    var roundId: Long,

    var teamId: Long,

    var value: Int,
) {
    constructor(roundInfoId: Long, teamId: Long, value: Int) : this(null, roundInfoId, teamId, value)
}

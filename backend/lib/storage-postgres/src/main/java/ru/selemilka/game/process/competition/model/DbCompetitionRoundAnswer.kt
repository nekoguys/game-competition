package ru.selemilka.game.process.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_round_answers")
data class DbCompetitionRoundAnswer(
    @Id
    var id: Long? = null,
    var roundInfoId: Long,
    var teamId: Long,
    var value: Int
)
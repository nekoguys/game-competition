package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_teams")
data class DbCompetitionTeam(
    @Id
    var id: Long?,
    var sessionId: Long,
    var teamNumber: Int,
    var name: String,
    var password: String,
    var banRound: Int?
)

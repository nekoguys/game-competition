package ru.selemilka.game.teams.competition.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("game_teams")
data class DbCompetitionTeam(
    @Id
    var teamId: Long?,
    var gameId: Long,
    var teamNumber: Int,
) {
    constructor(gameId: Long, teamNumber: Int) : this(null, gameId, teamNumber)
}
package ru.selemilka.game.teams.competition.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("team_members")
data class DbCompetitionTeamMember(
    @Id
    var id: Long?,
    var teamId: Long,
    var memberId: Long,
    @Column("captain")
    var captain: Boolean
) {
    constructor(teamId: Long, memberId: Long, captain: Boolean) : this(null, teamId, memberId, captain)
}
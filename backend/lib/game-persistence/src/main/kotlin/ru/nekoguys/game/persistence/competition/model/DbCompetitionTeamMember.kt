package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("team_members")
data class DbCompetitionTeamMember(
    @Id
    var id: Long?,
    var teamId: Long,
    var memberId: Long,
    var captain: Boolean
) {
    constructor(teamId: Long, memberId: Long, captain: Boolean) : this(null, teamId, memberId, captain)
}

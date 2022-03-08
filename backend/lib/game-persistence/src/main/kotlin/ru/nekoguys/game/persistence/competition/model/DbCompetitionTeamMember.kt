package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("competition_team_members")
data class DbCompetitionTeamMember(
    @Id
    var id: Long?,
    var userId: Long,
    var teamId: Long,
    var captain: Boolean,
)

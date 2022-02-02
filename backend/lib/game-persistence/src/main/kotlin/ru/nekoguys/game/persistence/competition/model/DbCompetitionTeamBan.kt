package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("banned_competition_teams")
data class DbCompetitionTeamBan(
    @Id
    var teamId: Long,
    var banRound: Int?
) : Persistable<Long> {

    override fun getId(): Long {
        return teamId
    }

    /**
     * Разбанить команду нельзя,
     * поэтому эта сущность отмечена как всегда новая
     */
    override fun isNew(): Boolean {
        return true
    }
}

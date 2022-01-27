package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("banned_competition_teams")
class DbCompetitionTeamBan private constructor(
    @Id
    var teamId: Long,
    var banRound: Int?
) : Persistable<Long> {
    @Transient
    var isNewTeam: Boolean = false

    override fun getId(): Long {
        return teamId
    }

    override fun isNew(): Boolean {
        return isNewTeam
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DbCompetitionTeamBan

        if (teamId != other.teamId) return false
        if (banRound != other.banRound) return false
        if (isNewTeam != other.isNewTeam) return false

        return true
    }

    override fun hashCode(): Int {
        var result = teamId.hashCode()
        result = 31 * result + (banRound ?: 0)
        result = 31 * result + isNewTeam.hashCode()
        return result
    }

    companion object {
        fun newTeamBan(teamId: Long, banRound: Int?): DbCompetitionTeamBan {
            return newTeam(teamId, banRound, true)
        }

        private fun newTeam(teamId: Long, banRound: Int?, isNewTeam: Boolean): DbCompetitionTeamBan {
            val teamBan = DbCompetitionTeamBan(teamId, banRound)
            teamBan.isNewTeam = isNewTeam
            return teamBan
        }
    }
}

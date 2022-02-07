package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.competition.model.CompetitionTeam

interface TeamRepository {
    fun getTeam(teamId: CompetitionTeam.Id): CompetitionTeam
    fun getTeam(teamId: Long): CompetitionTeam?

    companion object ResourceKey
}

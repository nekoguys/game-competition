package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.user.model.User

interface CompetitionPlayerRepository {
    suspend fun save(
        player: CompetitionPlayer.TeamMember,
        maxPlayers: Int = Int.MAX_VALUE,
    )

    suspend fun load(
        sessionId: CommonSession.Id,
        user: User,
    ): CompetitionPlayer

    fun loadAllInTeam(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionPlayer.TeamMember>

    fun loadAllInSession(
        sessionId: CommonSession.Id,
    ): Flow<CompetitionPlayer.TeamMember>

    companion object ResourceKey
}

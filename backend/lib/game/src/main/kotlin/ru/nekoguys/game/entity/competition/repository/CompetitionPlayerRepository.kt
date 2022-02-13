package ru.nekoguys.game.entity.competition.repository

import kotlinx.coroutines.flow.Flow
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.user.model.User

interface CompetitionPlayerRepository {
    suspend fun save(
        player: CompetitionPlayer.Student,
        maxPlayers: Int = Int.MAX_VALUE,
    )

    suspend fun load(
        sessionId: CommonSession.Id,
        user: User,
    ): CompetitionPlayer

    fun loadAllInTeam(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionPlayer.Student>

    fun loadAllInSession(
        sessionId: Long,
    ): Flow<CompetitionPlayer.Student>

    companion object ResourceKey
}

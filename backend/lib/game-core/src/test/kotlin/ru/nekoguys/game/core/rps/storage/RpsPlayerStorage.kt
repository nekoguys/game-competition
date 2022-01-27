package ru.nekoguys.game.core.rps.storage

import org.springframework.stereotype.Repository
import ru.nekoguys.game.core.LockableResource
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSession

interface RpsPlayerStorage {
    suspend fun loadPlayers(sessionId: RpsSession.Id): List<RpsPlayer.Human>
    suspend fun existsPlayer(player: RpsPlayer.Human): Boolean
    suspend fun savePlayer(player: RpsPlayer.Human)

    companion object ResourceKey : LockableResource()
}

@Repository
class RpsPlayerInMemoryStorage : RpsPlayerStorage {
    private val players = mutableSetOf<RpsPlayer.Human>()

    override suspend fun loadPlayers(sessionId: RpsSession.Id): List<RpsPlayer.Human> =
        players.filter { it.sessionId == sessionId }

    override suspend fun existsPlayer(player: RpsPlayer.Human): Boolean =
        player in players

    override suspend fun savePlayer(player: RpsPlayer.Human) {
        val isAdded = players.add(player)
        check(isAdded) { "Players cannot be added twice" }
    }
}

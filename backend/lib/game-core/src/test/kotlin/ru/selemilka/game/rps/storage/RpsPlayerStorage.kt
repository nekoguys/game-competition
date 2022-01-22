package ru.selemilka.game.rps.storage

import org.springframework.stereotype.Repository
import ru.selemilka.game.core.base.LockableResource
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession

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

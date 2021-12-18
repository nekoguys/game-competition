package ru.selemilka.game.rps.storage

import org.springframework.stereotype.Repository
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession

interface RpsPlayerStorage {
    suspend fun loadPlayers(sessionId: RpsSession.Id): List<RpsPlayer>
    suspend fun existsPlayer(player: RpsPlayer): Boolean
    suspend fun savePlayer(player: RpsPlayer)
}

@Repository
class RpsPlayerInMemoryStorage : RpsPlayerStorage {
    private val players = mutableSetOf<RpsPlayer>()

    override suspend fun loadPlayers(sessionId: RpsSession.Id): List<RpsPlayer> =
        players.filter { it.sessionId == sessionId }

    override suspend fun existsPlayer(player: RpsPlayer): Boolean =
        player in players

    override suspend fun savePlayer(player: RpsPlayer) {
        val isAdded = players.add(player)
        check(isAdded) { "Players cannot be added twice" }
    }
}

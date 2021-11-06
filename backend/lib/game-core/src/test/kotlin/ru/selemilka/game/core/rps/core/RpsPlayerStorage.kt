package ru.selemilka.game.core.rps.core

import ru.selemilka.game.core.base.SessionId

interface RpsPlayerStorage {
    sealed interface AddPlayerResult
    sealed interface AddPlayerFailure : AddPlayerResult
    object AddPlayerSuccess : AddPlayerResult
    object ThereAreAlreadyTwoPlayers : AddPlayerFailure
    object PlayerAlreadyJoinedGame : AddPlayerFailure

    suspend fun existsPlayer(id: SessionId, player: String): Boolean
    suspend fun addPlayer(id: SessionId, player: String): AddPlayerResult
}

class RpsInMemoryPlayerStorage : RpsPlayerStorage {
    class SessionPlayers() {
        val players = mutableListOf<String>()
    }

    private val sessionPlayerStorage = mutableMapOf<SessionId, SessionPlayers>()
    override suspend fun existsPlayer(id: SessionId, player: String): Boolean {
        return sessionPlayerStorage[id]?.players?.contains(player) ?: false
    }

    override suspend fun addPlayer(id: SessionId, player: String): RpsPlayerStorage.AddPlayerResult {
        val players = sessionPlayerStorage.computeIfAbsent(id) { SessionPlayers() }.players
        return if (existsPlayer(id, player)) {
            RpsPlayerStorage.PlayerAlreadyJoinedGame
        } else if (players.size >= 2) {
            RpsPlayerStorage.ThereAreAlreadyTwoPlayers
        } else {
            players.add(player)
            RpsPlayerStorage.AddPlayerSuccess
        }
    }
}
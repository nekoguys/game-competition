package ru.selemilka.game.rps.old.storage
//
//import ru.selemilka.game.core.rps.Session
//
//interface RpsPlayerStorage {
//    sealed interface AddPlayerResult
//    sealed interface AddPlayerFailure : AddPlayerResult
//    object AddPlayerSuccess : AddPlayerResult
//    object ThereAreAlreadyTwoPlayers : AddPlayerFailure
//    object PlayerAlreadyJoinedGame : AddPlayerFailure
//
//    suspend fun getPlayers(id: Session): List<String>
//
//    suspend fun existsPlayer(id: Session, player: String): Boolean
//    suspend fun addPlayer(id: Session, player: String): AddPlayerResult
//}
//
//class RpsInMemoryPlayerStorage : RpsPlayerStorage {
//    class SessionPlayers {
//        val players = mutableListOf<String>()
//    }
//
//    private val sessionPlayerStorage = mutableMapOf<Session, SessionPlayers>()
//
//    override suspend fun getPlayers(id: Session): List<String> {
//        return sessionPlayerStorage[id]?.players ?: emptyList()
//    }
//
//    override suspend fun existsPlayer(id: Session, player: String): Boolean {
//        return sessionPlayerStorage[id]?.players?.contains(player) ?: false
//    }
//
//    override suspend fun addPlayer(id: Session, player: String): RpsPlayerStorage.AddPlayerResult {
//        val players = sessionPlayerStorage.computeIfAbsent(id) { SessionPlayers() }.players
//        return when (players.size) {
//            in 0..1 -> {
//                when (existsPlayer(id, player)) {
//                    true -> RpsPlayerStorage.PlayerAlreadyJoinedGame
//                    false -> { players.add(player); return RpsPlayerStorage.AddPlayerSuccess
//                    }
//                }
//            }
//            else -> RpsPlayerStorage.ThereAreAlreadyTwoPlayers
//
//        }
//    }
//}

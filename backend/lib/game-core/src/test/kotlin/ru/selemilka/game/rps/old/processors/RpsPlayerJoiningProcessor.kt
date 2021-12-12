package ru.selemilka.game.rps.old.processors
//
//import ru.selemilka.game.core.base.ReactionScope
//import ru.selemilka.game.core.base.RpsSession.Id
//import ru.selemilka.game.core.base.TypedProcessor
//import rps.RpsPlayerCommand
//import rps.RpsPlayerMessage
//import ru.selemilka.game.core.rps.RpsPlayerScope
//import ru.selemilka.game.core.rps.storage.RpsPlayerStorage
//
//class RpsPlayerJoiningProcessor(
//    private val playerStorage: RpsPlayerStorage,
//) : TypedProcessor<RpsPlayerCommand.JoinGame, RpsPlayerMessage> {
//    override val actionClass = RpsPlayerCommand.JoinGame::class
//
//    override suspend fun process(id: RpsSession.Id, action: RpsPlayerCommand.JoinGame): List<RpsPlayerMessage> {
//        val player = action.initiator
//
//        return when (playerStorage.addPlayer(id, player.name)) {
//            RpsPlayerStorage.AddPlayerSuccess -> {
//                val youJoinedGame = RpsPlayerMessage.YouJoinedGame(
//                    scope = RpsPlayerScope(player),
//                    name = player.name,
//                )
//                val somebodyJoinedGame = RpsPlayerMessage.PlayerJoinedGame(
//                    scope = ReactionScope.All(id),
//                    name = player.name,
//                )
//                listOf(youJoinedGame, somebodyJoinedGame)
//            }
//
//            RpsPlayerStorage.ThereAreAlreadyTwoPlayers -> {
//                listOf(RpsPlayerMessage.ThereAreTwoPlayersInSession(scope = RpsPlayerScope(player)))
//            }
//
//            RpsPlayerStorage.PlayerAlreadyJoinedGame -> {
//                listOf(
//                    RpsPlayerMessage.PlayerAlreadyExists(
//                        scope = RpsPlayerScope(player),
//                        name = player.name,
//                    )
//                )
//            }
//        }
//    }
//}

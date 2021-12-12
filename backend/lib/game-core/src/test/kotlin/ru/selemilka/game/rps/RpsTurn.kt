package ru.selemilka.game.rps
//
//
//sealed interface RpsPlayerCommand : RpsCommand {
//    object CreateGame : ru.selemilka.game.rps.RpsPlayerCommand
//    object JoinGame : ru.selemilka.game.rps.RpsPlayerCommand
//    data class Turn(val decision: RpsTurn) : ru.selemilka.game.rps.RpsPlayerCommand
//}
//
//sealed interface RpsPlayerReaction : RpsMessage {
//    data class BadGameStage(
//        override val scope: RpsPlayerScope
//    ) : ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class PlayerAlreadyExists(
//        override val scope: RpsPlayerScope,
//        val name: String,
//    ) : ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class ThereAreTwoPlayersInSession(
//        override val scope: RpsPlayerScope,
//    ) : ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class YouJoinedGame(
//        override val scope: RpsPlayerScope,
//        val name: String,
//    ) : ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class PlayerJoinedGame(
//        override val scope: ReactionScope.All,
//        val name: String,
//    ) : ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class PlayerMadeTurn(
//        val turn: ru.selemilka.game.rps.RpsPlayerCommand.Turn
//    ) : _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction {
//        override val scope: RpsPlayerScope
//            get() = RpsPlayerScope(turn.initiator)
//    }
//
//    data class PlayerTurnFailed(
//        override val scope: RpsPlayerScope,
//        val reason: String
//    ) : _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction
//
//    data class RoundResult(
//        override val scope: ReactionScope.All,
//        val result: _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.RoundResult.RoundResult
//    ) : _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction {
//        sealed interface RoundResult
//        object Draw : _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.RoundResult.RoundResult
//        data class Winner(val winner: String) :
//            _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.RoundResult.RoundResult
//    }
//}

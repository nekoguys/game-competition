package ru.selemilka.game.core.rps



sealed interface RpsPlayerCommand : RpsCommand {
    object CreateGame : RpsPlayerCommand
    object JoinGame : RpsPlayerCommand
    data class Turn(val decision: RpsTurn) : RpsPlayerCommand
}

sealed interface RpsPlayerReaction : RpsMessage {
    data class BadGameStage(
        override val scope: RpsPlayerScope
    ) : RpsPlayerReaction

    data class PlayerAlreadyExists(
        override val scope: RpsPlayerScope,
        val name: String,
    ) : RpsPlayerReaction

    data class ThereAreTwoPlayersInSession(
        override val scope: RpsPlayerScope,
    ) : RpsPlayerReaction

    data class YouJoinedGame(
        override val scope: RpsPlayerScope,
        val name: String,
    ) : RpsPlayerReaction

    data class PlayerJoinedGame(
        override val scope: ReactionScope.All,
        val name: String,
    ) : RpsPlayerReaction

    data class PlayerMadeTurn(
        val turn: RpsPlayerCommand.Turn
    ) : RpsPlayerReaction {
        override val scope: RpsPlayerScope
            get() = RpsPlayerScope(turn.initiator)
    }

    data class PlayerTurnFailed(
        override val scope: RpsPlayerScope,
        val reason: String
    ) : RpsPlayerReaction

    data class RoundResult(
        override val scope: ReactionScope.All,
        val result: RoundResult
    ) : RpsPlayerReaction {
        sealed interface RoundResult
        object Draw : RoundResult
        data class Winner(val winner: String) : RoundResult
    }
}

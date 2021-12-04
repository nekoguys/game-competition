package ru.selemilka.game.core.rps

import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId

enum class RockPaperScissorsTurn {
    Rock, Paper, Scissors
}

sealed interface RpsPlayerAction : RpsAction {
    data class CreateGame(override val initiator: RpsPlayer) : RpsPlayerAction

    data class JoinGame(override val initiator: RpsPlayer) : RpsPlayerAction

    data class Turn(override val initiator: RpsPlayer, val decision: RockPaperScissorsTurn) : RpsPlayerAction
}

sealed interface RpsPlayerReaction : RpsReaction {
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
        val turn: RpsPlayerAction.Turn
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

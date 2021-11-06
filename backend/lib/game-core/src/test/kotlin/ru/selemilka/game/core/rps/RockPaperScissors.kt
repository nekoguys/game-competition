package ru.selemilka.game.core.rps

import ru.selemilka.game.core.base.ReactionScope

sealed interface RpsPlayerAction : RpsAction {
    data class JoinGame(override val initiator: RpsPlayer) : RpsPlayerAction
}

sealed interface RpsPlayerReaction : RpsReaction {
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
}

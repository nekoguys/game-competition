package ru.selemilka.game.core.rps

import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor

sealed interface RpsPlayerAction : RpsAction {
    data class JoinGame(override val initiator: RpsPlayer) : RpsPlayerAction
}

sealed interface RpsPlayerReaction : RpsReaction {
    data class PlayerAlreadyExists(
        override val scope: RpsPlayerScope,
        val name: String,
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

interface RpsPlayerStorage {
    suspend fun existsPlayer(id: SessionId, player: String): Boolean
    suspend fun addPlayer(id: SessionId, player: String)
}

class RpsPlayerJoiningProcessor(
    private val playerStorage: RpsPlayerStorage,
) : TypedProcessor<RpsPlayerAction.JoinGame, RpsPlayerReaction> {
    override val actionClass = RpsPlayerAction.JoinGame::class

    override suspend fun process(id: SessionId, action: RpsPlayerAction.JoinGame): List<RpsPlayerReaction> {
        val player = action.initiator

        return if (playerStorage.existsPlayer(id, player.name)) {
            val error = RpsPlayerReaction.PlayerAlreadyExists(
                scope = RpsPlayerScope(player),
                name = player.name,
            )
            listOf(error)
        } else {
            playerStorage.addPlayer(id, player.name)
            val youJoinedGame = RpsPlayerReaction.YouJoinedGame(
                scope = RpsPlayerScope(player),
                name = player.name,
            )
            val somebodyJoinedGame = RpsPlayerReaction.PlayerJoinedGame(
                scope = ReactionScope.All(id),
                name = player.name,
            )
            listOf(youJoinedGame, somebodyJoinedGame)
        }
    }
}

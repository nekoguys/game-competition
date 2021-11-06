package ru.selemilka.game.core.rps.core.processors

import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import ru.selemilka.game.core.rps.RpsPlayerAction
import ru.selemilka.game.core.rps.RpsPlayerReaction
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.core.RpsPlayerStorage

class RpsPlayerJoiningProcessor(
    private val playerStorage: RpsPlayerStorage,
) : TypedProcessor<RpsPlayerAction.JoinGame, RpsPlayerReaction> {
    override val actionClass = RpsPlayerAction.JoinGame::class

    override suspend fun process(id: SessionId, action: RpsPlayerAction.JoinGame): List<RpsPlayerReaction> {
        val player = action.initiator

        return when (playerStorage.addPlayer(id, player.name)) {
            RpsPlayerStorage.AddPlayerSuccess -> {
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

            RpsPlayerStorage.ThereAreAlreadyTwoPlayers -> {
                listOf(RpsPlayerReaction.ThereAreTwoPlayersInSession(scope = RpsPlayerScope(player)))
            }

            RpsPlayerStorage.PlayerAlreadyJoinedGame -> {
                listOf(
                    RpsPlayerReaction.PlayerAlreadyExists(
                        scope = RpsPlayerScope(player),
                        name = player.name,
                    )
                )
            }
        }
    }
}
package ru.selemilka.game.core.rps.core.processors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.rps.RpsPlayer
import ru.selemilka.game.core.rps.RpsPlayerAction
import ru.selemilka.game.core.rps.RpsPlayerReaction
import ru.selemilka.game.core.rps.RpsPlayerScope
import ru.selemilka.game.core.rps.core.RpsInMemoryPlayerStorage

internal class RpsPlayerJoiningProcessorTests {
    private val processor = RpsPlayerJoiningProcessor(playerStorage = RpsInMemoryPlayerStorage())
    private val sessionId = 1L

    @Test
    fun `Two players successfully joined game`() {
        val result = joinGame(sessionId, RpsPlayer("player1"))
        assertEquals(
            expectedReactionOnJoiningGame(RpsPlayer("player1"), sessionId),
            result
        )

        val result2 = joinGame(sessionId, RpsPlayer("player2"))
        assertEquals(
            expectedReactionOnJoiningGame(RpsPlayer("player2"), sessionId),
            result2
        )
    }

    @Test
    fun `fail when player tries to join second time`() {
        val result = joinGame(sessionId, RpsPlayer("player1"))
        assertEquals(expectedReactionOnJoiningGame(RpsPlayer("player1"), sessionId), result)

        val result2 = joinGame(sessionId, RpsPlayer("player1"))
        assertEquals(
            listOf(
                RpsPlayerReaction.PlayerAlreadyExists(scope = RpsPlayerScope(RpsPlayer("player1")), name = "player1"),
            ),
            result2
        )
    }

    @Test
    fun `fail when there are already two players`() {
        joinGame(sessionId, RpsPlayer("player1"))
        joinGame(sessionId, RpsPlayer("player2"))
        val result = joinGame(sessionId, RpsPlayer("player3"))
        assertEquals(
            listOf(
                RpsPlayerReaction.ThereAreTwoPlayersInSession(scope = RpsPlayerScope(RpsPlayer("player3")))
            ),
            result
        )
    }

    private fun joinGame(sessionId: SessionId, initiator: RpsPlayer) =
        runBlocking { processor.process(id = sessionId, action = RpsPlayerAction.JoinGame(initiator)) }

    private fun expectedReactionOnJoiningGame(player: RpsPlayer, sessionId: SessionId): List<RpsPlayerReaction> {
        return listOf(
            RpsPlayerReaction.YouJoinedGame(
                RpsPlayerScope(initiator = player),
                name = player.name
            ),
            RpsPlayerReaction.PlayerJoinedGame(
                ReactionScope.All(sessionId = sessionId),
                name = player.name
            )
        )
    }
}
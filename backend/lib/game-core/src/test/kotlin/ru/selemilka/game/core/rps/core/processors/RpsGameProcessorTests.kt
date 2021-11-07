package ru.selemilka.game.core.rps.core.processors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.rps.RockPaperScissorsTurn
import ru.selemilka.game.core.rps.RpsPlayer
import ru.selemilka.game.core.rps.RpsPlayerAction
import ru.selemilka.game.core.rps.RpsPlayerReaction
import ru.selemilka.game.core.rps.core.RpsGameStateInMemoryStorage

class RpsGameProcessorTests {
    private val processor = RpsGameProcessor(RpsGameStateInMemoryStorage())
    private val sessionId = 1L

    @Test
    fun `only one answer submitted and expect no result`() {
        val result = submitTurn(sessionId, "player1", RockPaperScissorsTurn.Rock)
        assertEquals(result.size, 1)
        val reaction = result.first()
        assertTrue(reaction is RpsPlayerReaction.PlayerMadeTurn)
        val playerMadeTurn = reaction as RpsPlayerReaction.PlayerMadeTurn
        assertEquals(playerMadeTurn.turn.initiator.name, "player1")
        assertEquals(playerMadeTurn.turn.decision, RockPaperScissorsTurn.Rock)
    }

    @Test
    fun `one player submitted answer 2 times`() {
        submitTurn(sessionId, "player1", RockPaperScissorsTurn.Rock)
        val result = submitTurn(sessionId, "player1", RockPaperScissorsTurn.Scissors)
        assertEquals(result.size, 1)
        val reaction = result.first()
        assertTrue(reaction is RpsPlayerReaction.PlayerTurnFailed)
    }

    @Test
    fun `all players submitted and expect draw`() {
        submitTurn(sessionId, "player1", RockPaperScissorsTurn.Rock)
        val result = submitTurn(sessionId, "player2", RockPaperScissorsTurn.Rock)
        assertEquals(result.size, 2)
        val reaction = result[1]
        assertTrue(reaction is RpsPlayerReaction.RoundResult)
        assertTrue((reaction as RpsPlayerReaction.RoundResult).result is RpsPlayerReaction.RoundResult.Draw)
    }

    @Test
    fun `all players submitted and expect win first`() {
        submitTurn(sessionId, "player1", RockPaperScissorsTurn.Rock)
        val result = submitTurn(sessionId, "player2", RockPaperScissorsTurn.Scissors)
        assertEquals(result.size, 2)
        val reaction = result[1]
        assertTrue(reaction is RpsPlayerReaction.RoundResult)
        val roundResult = (reaction as RpsPlayerReaction.RoundResult).result
        assertTrue(roundResult is RpsPlayerReaction.RoundResult.Winner)
        assertEquals((roundResult as RpsPlayerReaction.RoundResult.Winner).winner, "player1")
    }


    private fun submitTurn(id: SessionId, player: String, turn: RockPaperScissorsTurn): List<RpsPlayerReaction> =
        runBlocking { processor.process(id, RpsPlayerAction.Turn(initiator = RpsPlayer(player), decision = turn)) }
}
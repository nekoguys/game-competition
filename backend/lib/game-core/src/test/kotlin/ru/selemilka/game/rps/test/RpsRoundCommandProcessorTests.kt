package ru.selemilka.game.rps.test

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.selemilka.game.core.base.RpsSession.Id
import rps.RpsTurn
import rps.RpsPlayer
import rps.RpsPlayerCommand
import rps.RpsPlayerMessage
import ru.selemilka.game.core.rps.processors.RpsGameProcessor
import ru.selemilka.game.core.rps.storage.RpsGameStateInMemoryStorage

class RpsRoundCommandProcessorTests {
    private val processor = RpsGameProcessor(RpsGameStateInMemoryStorage())
    private val sessionId = 1L

    @Test
    fun `only one answer submitted and expect no result`() {
        val result = submitTurn(sessionId, "player1", RpsTurn.ROCK)
        assertEquals(result.size, 1)
        val reaction = result.first()
        assertTrue(reaction is RpsPlayerMessage.PlayerMadeTurn)
        val playerMadeTurn = reaction as RpsPlayerMessage.PlayerMadeTurn
        assertEquals(playerMadeTurn.turn.initiator.name, "player1")
        assertEquals(playerMadeTurn.turn.decision, RpsTurn.ROCK)
    }

    @Test
    fun `one player submitted answer 2 times`() {
        submitTurn(sessionId, "player1", RpsTurn.ROCK)
        val result = submitTurn(sessionId, "player1", RpsTurn.SCISSORS)
        assertEquals(result.size, 1)
        val reaction = result.first()
        assertTrue(reaction is RpsPlayerMessage.PlayerTurnFailed)
    }

    @Test
    fun `all players submitted and expect draw`() {
        submitTurn(sessionId, "player1", RpsTurn.ROCK)
        val result = submitTurn(sessionId, "player2", RpsTurn.ROCK)
        assertEquals(result.size, 2)
        val reaction = result[1]
        assertTrue(reaction is RpsPlayerMessage.RoundResult)
        assertTrue((reaction as RpsPlayerMessage.RoundResult).result is RpsPlayerMessage.RoundResult.Draw)
    }

    @Test
    fun `all players submitted and expect win first`() {
        submitTurn(sessionId, "player1", RpsTurn.ROCK)
        val result = submitTurn(sessionId, "player2", RpsTurn.SCISSORS)
        assertEquals(result.size, 2)
        val reaction = result[1]
        assertTrue(reaction is RpsPlayerMessage.RoundResult)
        val roundResult = (reaction as RpsPlayerMessage.RoundResult).result
        assertTrue(roundResult is RpsPlayerMessage.RoundResult.Winner)
        assertEquals((roundResult as RpsPlayerMessage.RoundResult.Winner).winner, "player1")
    }


    private fun submitTurn(id: RpsSession.Id, player: String, turn: RpsTurn): List<RpsPlayerMessage> =
        runBlocking { processor.process(id, RpsPlayerCommand.Turn(initiator = RpsPlayer(player), decision = turn)) }
}

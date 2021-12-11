package ru.selemilka.game.core.rps.processors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.rps.*
import ru.selemilka.game.core.rps.storage.RpsGameStateInMemoryStorage
import ru.selemilka.game.core.rps.storage.RpsInMemoryPlayerStorage
import ru.selemilka.game.core.rps.storage.RpsRootStateInMemoryStorage

internal class RpsRootProcessorTest {
    private val rpsRootProcessor = RpsRootProcessor(
            RpsGameStateInMemoryStorage(),
            RpsInMemoryPlayerStorage(),
            RpsRootStateInMemoryStorage()
    )
    @Test
    fun `test correct flow`() {
        val id = 1L
        val player1 = RpsPlayer("player1")
        val player2 = RpsPlayer("player2")
        process(id, RpsPlayerCommand.CreateGame(player1))
        val result = process(id, RpsPlayerCommand.JoinGame(player1)).toMutableList()
        result += process(id, RpsPlayerCommand.JoinGame(player2))
        assertTrue(result.contains(RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player1.name)))
        assertTrue(result.contains(RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player2.name)))
        assertTrue(result.contains(RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player1), player1.name)))
        assertTrue(result.contains(RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player2), player2.name)))

        process(id, RpsPlayerCommand.Turn(player1, RpsTurn.ROCK))
        val lastTurnResult = process(id, RpsPlayerCommand.Turn(player2, RpsTurn.PAPER))
        assertTrue(lastTurnResult.contains(RpsPlayerReaction.RoundResult(ReactionScope.All(id), RpsPlayerReaction.RoundResult.Winner(player2.name))))
    }

    private fun process(id: SessionId, action: RpsPlayerCommand): List<RpsPlayerReaction> {
        return runBlocking { rpsRootProcessor.process(id, action) }
    }
}

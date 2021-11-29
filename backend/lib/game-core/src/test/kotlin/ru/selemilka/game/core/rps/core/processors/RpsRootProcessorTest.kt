package ru.selemilka.game.core.rps.core.processors

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.selemilka.game.core.base.ReactionScope
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.rps.*
import ru.selemilka.game.core.rps.core.RpsGameStateInMemoryStorage
import ru.selemilka.game.core.rps.core.RpsInMemoryPlayerStorage
import ru.selemilka.game.core.rps.core.RpsRootStageStorage
import ru.selemilka.game.core.rps.core.RpsRootStateInMemoryStorage

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
        process(id, RpsPlayerAction.CreateGame(player1))
        val result = process(id, RpsPlayerAction.JoinGame(player1)).toMutableList()
        result += process(id, RpsPlayerAction.JoinGame(player2))
        assertTrue(result.contains(RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player1.name)))
        assertTrue(result.contains(RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player2.name)))
        assertTrue(result.contains(RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player1), player1.name)))
        assertTrue(result.contains(RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player2), player2.name)))

        process(id, RpsPlayerAction.Turn(player1, RockPaperScissorsTurn.Rock))
        val lastTurnResult = process(id, RpsPlayerAction.Turn(player2, RockPaperScissorsTurn.Paper))
        assertTrue(lastTurnResult.contains(RpsPlayerReaction.RoundResult(ReactionScope.All(id), RpsPlayerReaction.RoundResult.Winner(player2.name))))
    }

    private fun process(id: SessionId, action: RpsPlayerAction): List<RpsPlayerReaction> {
        return runBlocking { rpsRootProcessor.process(id, action) }
    }
}
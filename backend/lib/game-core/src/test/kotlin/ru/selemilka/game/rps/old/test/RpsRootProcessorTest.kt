package ru.selemilka.game.rps.old.test

//
//internal class RpsRootProcessorTest {
//    private val rpsRootProcessor = RpsRootProcessor(
//        RpsGameStateInMemoryStorage(),
//        RpsInMemoryPlayerStorage(),
//        RpsRootStateInMemoryStorage()
//    )
//
//    @Test
//    fun `test correct flow`() {
//        val id = 1L
//        val player1 = RpsPlayer("player1")
//        val player2 = RpsPlayer("player2")
//        process(id, _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand.CreateGame(player1))
//        val result = process(id, _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand.JoinGame(player1)).toMutableList()
//        result += process(id, _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand.JoinGame(player2))
//        assertTrue(result.contains(_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player1.name)))
//        assertTrue(result.contains(_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.PlayerJoinedGame(ReactionScope.All(id), player2.name)))
//        assertTrue(result.contains(_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player1), player1.name)))
//        assertTrue(result.contains(_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.YouJoinedGame(RpsPlayerScope(player2), player2.name)))
//
//        process(id, _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand.Turn(player1, RpsTurn.ROCK))
//        val lastTurnResult = process(id, _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand.Turn(player2, RpsTurn.PAPER))
//        assertTrue(lastTurnResult.contains(_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.RoundResult(ReactionScope.All(id),
//            _root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction.RoundResult.Winner(player2.name))))
//    }
//
//    private fun process(id: RpsSession.Id, action: _root_ide_package_.ru.selemilka.game.rps.RpsPlayerCommand): List<_root_ide_package_.ru.selemilka.game.rps.RpsPlayerReaction> {
//        return runBlocking { rpsRootProcessor.process(id, action) }
//    }
//}

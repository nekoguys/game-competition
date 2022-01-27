package ru.nekoguys.game.core.rps.test

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.core.rps.RpsGameCoreTest
import ru.nekoguys.game.core.rps.RpsGameService
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSessionSettings
import ru.nekoguys.game.core.rps.rule.JoinGameMessageError
import ru.nekoguys.game.core.rps.rule.RpsChangeStageMessage
import ru.nekoguys.game.core.rps.rule.RpsCommand
import ru.nekoguys.game.core.rps.rule.RpsJoinGameMessage
import ru.nekoguys.game.core.session.accept
import ru.nekoguys.game.core.session.close
import ru.nekoguys.game.core.session.getMessages

@RpsGameCoreTest
class JoinGameRuleTest @Autowired constructor(
    private val gameService: RpsGameService
) {

    @Test
    fun `can join when session is empty`(): Unit = runBlocking {
        val session = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val firstPlayer = RpsPlayer.Human(session.id, "Max")

        session.accept(firstPlayer, RpsCommand.JoinGame)
        session.close()

        val responses = session.getMessages(firstPlayer).toList()
        assertThat(responses)
            .containsExactly(
                RpsJoinGameMessage.YouJoinedGame,
            )
    }

    @Test
    fun `cannot join when session is full`(): Unit = runBlocking {
        val session = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val extraPlayer = RpsPlayer.Human(session.id, "I am late, guys")

        session.accept(RpsPlayer.Human(session.id, "Max"), RpsCommand.JoinGame)
        session.accept(RpsPlayer.Human(session.id, "Serega"), RpsCommand.JoinGame)
        session.accept(extraPlayer, RpsCommand.JoinGame)
        session.close()

        assertThat(session.getMessages(extraPlayer).toList())
            .containsExactly(
                JoinGameMessageError.SessionIsFull,
            )
    }

    @Test
    fun `game is started when all players joined`(): Unit = runBlocking {
        val session = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val firstPlayer = RpsPlayer.Human(session.id, "Max")
        val secondPlayer = RpsPlayer.Human(session.id, "Christian")

        session.accept(firstPlayer, RpsCommand.JoinGame)
        session.accept(secondPlayer, RpsCommand.JoinGame)
        session.close()

        assertThat(session.getMessages(firstPlayer).toList())
            .describedAs("first player responses")
            .contains(
                RpsChangeStageMessage.GameStarted
            )

        assertThat(session.getMessages(secondPlayer).toList())
            .describedAs("second player responses")
            .contains(
                RpsChangeStageMessage.GameStarted
            )
    }
}

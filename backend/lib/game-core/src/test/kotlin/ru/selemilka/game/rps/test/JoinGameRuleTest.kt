package ru.selemilka.game.rps.test

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.core.base.accept
import ru.selemilka.game.core.base.close
import ru.selemilka.game.core.base.getMessages
import ru.selemilka.game.rps.RpsGameConfiguration
import ru.selemilka.game.rps.RpsGameService
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.ChangeStageMessage
import ru.selemilka.game.rps.rule.JoinGameMessage
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.toRoot

@SpringBootTest(classes = [RpsGameConfiguration::class])
class JoinGameRuleTest {

    @Autowired
    lateinit var gameService: RpsGameService

    @Test
    fun `can join when session is empty`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val firstPlayer = RpsPlayer.Human(sessionId, "Max")

        launch {
            session.accept(firstPlayer, RpsCommand.JoinGame)
            session.close()
        }

        val responses = session.getMessages(firstPlayer).toList()
        assertThat(responses)
            .containsExactly(
                JoinGameMessage.YouJoinedGame.toRoot(),
            )
    }

    @Test
    fun `cannot join when session is full`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val extraPlayer = RpsPlayer.Human(sessionId, "I am late, guys")

        launch {
            session.accept(RpsPlayer.Human(sessionId, "Max"), RpsCommand.JoinGame)
            session.accept(RpsPlayer.Human(sessionId, "Serega"), RpsCommand.JoinGame)
            session.accept(extraPlayer, RpsCommand.JoinGame)
            session.close()
        }

        assertThat(session.getMessages(extraPlayer).toList())
            .containsExactly(
                JoinGameMessage.SessionIsFull.toRoot(),
            )
    }

    @Test
    fun `game is started when all players joined`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings(maxPlayers = 2))
        val firstPlayer = RpsPlayer.Human(sessionId, "Max")
        val secondPlayer = RpsPlayer.Human(sessionId, "Christian")

        launch {
            session.accept(firstPlayer, RpsCommand.JoinGame)
            session.accept(secondPlayer, RpsCommand.JoinGame)
            session.close()
        }

        assertThat(session.getMessages(firstPlayer).toList())
            .describedAs("first player responses")
            .contains(
                ChangeStageMessage.GameStarted.toRoot()
            )

        assertThat(session.getMessages(secondPlayer).toList())
            .describedAs("second player responses")
            .contains(
                ChangeStageMessage.GameStarted.toRoot()
            )
    }
}

package ru.selemilka.game.rps.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.accept
import ru.selemilka.game.core.base.close
import ru.selemilka.game.rps.RpsGameConfiguration
import ru.selemilka.game.rps.RpsGameService
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.JoinGameMessage
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.toRoot

/**
 * Тесты на [RpsGameService]
 *
 * Это интеграционный тест, здесь тестируются не отдельные игровые правила,
 * а в целом работа всего сервиса
 */
@SpringBootTest(classes = [RpsGameConfiguration::class])
class RpsGameServiceTest {

    @Autowired
    lateinit var gameService: RpsGameService

    @Test
    fun `can send commands and then receive messages`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings())
        val player = RpsPlayer.Human(sessionId, "Max")

        session.accept(player, RpsCommand.JoinGame)
        session.close()

        val responses = session.getAllMessages().toList()
        assertThat(responses)
            .containsExactly(GameMessage(player, JoinGameMessage.YouJoinedGame.toRoot()))
    }

    @Test
    fun `can send commands in different coroutine`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings())
        val player = RpsPlayer.Human(sessionId, "Max")

        launch {
            session.accept(player, RpsCommand.JoinGame)
            session.close()
        }

        val responses = session.getAllMessages().toList()
        assertThat(responses)
            .containsExactly(GameMessage(player, JoinGameMessage.YouJoinedGame.toRoot()))
    }

    @Test
    fun `can send commands in parallel`(): Unit = runBlocking {
        val (sessionId, session) = gameService.startSession(RpsSessionSettings(maxPlayers = 3))
        val players = List(3) {
            RpsPlayer.Human(sessionId, "Player #$it")
        }

        launch {
            launch {
                session.accept(players[0], RpsCommand.JoinGame)
            }
            launch {
                session.accept(players[1], RpsCommand.JoinGame)
            }
            launch {
                session.accept(players[2], RpsCommand.JoinGame)
            }
            launch {
                delay(100)
                session.close()
            }
        }

        val responses = session.getAllMessages().toList()
        assertThat(responses)
            .contains(
                GameMessage(players[0], JoinGameMessage.YouJoinedGame.toRoot()),
                GameMessage(players[1], JoinGameMessage.YouJoinedGame.toRoot()),
                GameMessage(players[2], JoinGameMessage.YouJoinedGame.toRoot()),
            )
    }
}

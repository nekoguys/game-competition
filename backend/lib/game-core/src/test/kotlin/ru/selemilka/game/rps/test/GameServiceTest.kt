package ru.selemilka.game.rps.test

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.core.base.TargetedMessage
import ru.selemilka.game.rps.RpsGameConfiguration
import ru.selemilka.game.rps.RpsGameService
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rules.JoinGameMessage
import ru.selemilka.game.rps.rules.RpsRootCommand
import ru.selemilka.game.rps.rules.intoRoot

/**
 * Тесты на [RpsGameService]
 *
 * Это интеграционный тест, здесь тестируются не отдельные игровые правила,
 * а в целом работа всего сервиса
 */
@SpringBootTest(classes = [RpsGameConfiguration::class])
class GameServiceTest {

    @Autowired
    lateinit var gameService: RpsGameService

    @Test
    fun `player can join when session is empty`(): Unit = runBlocking {
        // given
        val sessionId = gameService.start(RpsSessionSettings())
        val player = RpsPlayer(sessionId, "Max")

        // when
        launch {
            gameService.accept(player, RpsRootCommand.JoinGame)
            gameService.stop(sessionId)
        }

        // then
        val responses = gameService.getAllMessages(sessionId).toList()
        assertThat(responses)
            .containsExactly(TargetedMessage(player, JoinGameMessage.YouJoinedGame.intoRoot()))
    }

    @Test
    fun `player cannot join when session is full`(): Unit = runBlocking {
        // given
        val sessionId = gameService.start(RpsSessionSettings())
        val extraPlayer = RpsPlayer(sessionId, "I am late guys")

        // when
        launch {
            gameService.accept(RpsPlayer(sessionId, "Max"), RpsRootCommand.JoinGame)
            gameService.accept(RpsPlayer(sessionId, "Serega"), RpsRootCommand.JoinGame)
            gameService.accept(extraPlayer, RpsRootCommand.JoinGame)
            gameService.stop(sessionId)
        }

        // then
        val messages = gameService.getMessages(extraPlayer).toList()
        assertThat(messages)
            .containsExactly(JoinGameMessage.SessionIsFull.intoRoot())
    }
}

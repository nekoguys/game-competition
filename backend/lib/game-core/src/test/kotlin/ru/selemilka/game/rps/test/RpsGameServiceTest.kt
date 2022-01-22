package ru.selemilka.game.rps.test

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.session.accept
import ru.selemilka.game.core.session.close
import ru.selemilka.game.core.session.getAllMessages
import ru.selemilka.game.rps.RpsGameConfiguration
import ru.selemilka.game.rps.RpsGameService
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsJoinGameMessage

/**
 * Тесты на [RpsGameService]
 *
 * Здесь тестируются не отдельные игровые правила,
 * а в целом работа всего сервиса
 */
@SpringBootTest(classes = [RpsGameConfiguration::class])
class RpsGameServiceTest {

    @Autowired
    lateinit var gameService: RpsGameService

    @Test
    fun `can send commands and then receive messages`(): Unit = runBlocking {
        val session = gameService.startSession(RpsSessionSettings())
        val player = RpsPlayer.Human(session.id, "Max")

        session.accept(player, RpsCommand.JoinGame)
        session.close()

        assertThat(session.getAllMessages().toList())
            .containsExactly(
                GameMessage(player, RpsJoinGameMessage.YouJoinedGame),
            )
    }

    @Test
    fun `can send commands in different coroutine`(): Unit = runBlocking {
        val session = gameService.startSession(RpsSessionSettings())
        val player = RpsPlayer.Human(session.id, "Max")

        launch {
            session.accept(player, RpsCommand.JoinGame)
            session.close()
        }

        assertThat(session.getAllMessages().toList())
            .containsExactly(
                GameMessage(player, RpsJoinGameMessage.YouJoinedGame),
            )
    }

    @Test
    fun `can send commands in parallel`(): Unit = runBlocking {
        val settings = RpsSessionSettings(maxPlayers = 100)
        val session = gameService.startSession(settings)
        val players = List(settings.maxPlayers) {
            RpsPlayer.Human(session.id, "Player #$it")
        }

        launch {
            players
                .map { player ->
                    async { session.accept(player, RpsCommand.JoinGame) }
                }
                .awaitAll()
            launch {
                session.close()
            }
        }

        assertThat(session.getAllMessages().toList())
            .containsOnlyOnceElementsOf(
                players.map { GameMessage(it, RpsJoinGameMessage.YouJoinedGame) }
            )
    }
}

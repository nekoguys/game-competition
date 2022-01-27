package ru.nekoguys.game.core.rps.test

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.rps.RpsGameCoreTest
import ru.nekoguys.game.core.rps.RpsGameService
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSessionSettings
import ru.nekoguys.game.core.rps.rule.RpsCommand
import ru.nekoguys.game.core.rps.rule.RpsJoinGameMessage
import ru.nekoguys.game.core.session.accept
import ru.nekoguys.game.core.session.close
import ru.nekoguys.game.core.session.getAllMessages

/**
 * Тесты на [RpsGameService]
 *
 * Здесь тестируются не отдельные игровые правила,
 * а в целом работа всего сервиса
 */
@RpsGameCoreTest
class RpsGameServiceTes @Autowired constructor(
    private val gameService: RpsGameService,
) {

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

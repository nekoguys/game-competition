package ru.selemilka.game.rps.test

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.core.base.Game
import ru.selemilka.game.rps.RpsGameConfiguration
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.rules.RpsRootCommand
import ru.selemilka.game.rps.rules.RpsRootMessage
import ru.selemilka.game.rps.rules.RpsRootRule

@SpringBootTest(classes = [RpsGameConfiguration::class])
class RootRuleTest {
    @Autowired
    lateinit var rootRule: RpsRootRule

    @Test
    fun `session does not exists`(): Unit = runBlocking {
        val gameCoroutine = Game(rootRule::process)
        val player = RpsPlayer(sessionId = RpsSession.Id(42), name = "Toha")

        launch {
            gameCoroutine.accept(player, RpsRootCommand.JoinGame)
            gameCoroutine.stop()
        }

        val responses =
            gameCoroutine
                .getMessages(player)
                .toList()

        assertThat(responses)
            .containsExactly(RpsRootMessage.SessionDoesNotExists)
    }
}

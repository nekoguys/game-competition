package ru.selemilka.game.rps.test

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.selemilka.game.rps.RpsGame
import ru.selemilka.game.rps.RpsGameConfiguration

@SpringBootTest(classes = [RpsGameConfiguration::class])
class RootCommandTest {
    @Autowired
    lateinit var game: RpsGame

    @Test
    fun test() {
        game.close()
    }
}

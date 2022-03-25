package ru.nekoguys.game.web.service

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@Disabled // в рамках CI не запускается, только руками
@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class ManualPerformanceTest @Autowired constructor(
    private val game: TestGame,
) {

    @Test
    @RepeatedTest(value = 20)
    fun `whole game process`() {
        val request = TestGame.DEFAULT_CREATE_COMPETITION_REQUEST
            .copy(
                teamLossUpperbound = Int.MAX_VALUE,
                roundsCount = 20
            )

        val pin = game.createSession(request = request)
        val captains = List(20) { game.createUser() }
        captains.forEach {
            val (_, teamName) = game.createTeam(pin, it)
            for (i in 0 until 15) {
                game.joinTeam(pin, teamName)
            }
        }

        game.startCompetition(pin)
        for (i in 0 until 20) {
            game.startRound(pin)
            for (c in captains) {
                game.submitAnswer(pin, c, 0)
            }
            game.endRound(pin)
        }
    }
}

package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class CompetitionProcessTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {
    @Test
    fun f() {
        val studentA = game.createUser()
        val studentB = game.createUser()
        val competitionPin = game.createCompetition()
            .also { pin ->
                game.joinTeam(pin, teamMember = studentA)
                game.joinTeam(pin, teamMember = studentB)
                game.startCompetition(pin)
            }

    }
}

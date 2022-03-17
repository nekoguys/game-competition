package ru.nekoguys.game.web.service.process

import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.competition.rule.CompetitionStageChangedMessage
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.util.concurrent.TimeUnit
import ru.nekoguys.game.entity.competition.CompetitionProcessService as CoreCompetitionProcessService

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class StartRoundServiceTest @Autowired constructor(
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val game: TestGame,
) {

    @Test
    fun `stage changes when round starts`() {
        val session = game.createAndLoadSession { pin ->
            repeat(2) { game.createTeam(pin) }
            game.startCompetition(pin)
        }
        assertThat(session.stage)
            .isEqualTo(CompetitionStage.WaitingStart(1))

        game.startRound(session.pin)

        val sessionAfter = game.loadSession(session.pin)
        assertThat(sessionAfter.stage)
            .isEqualTo(CompetitionStage.InProcess(round = 1))
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `event is sent when round starts`() {
        val session = game.createAndLoadSession { pin ->
            repeat(2) { game.createTeam(pin) }
            game.startCompetition(pin)
        }
        assertThat(session.stage)
            .isEqualTo(CompetitionStage.WaitingStart(1))

        game.startRound(session.pin)
        val generatedMessage = runBlocking {
            // если что-то пойдёт не так при получении ивента,
            // тест может зависнуть намертво
            // поэтому в тесте проставлен таймаут
            coreCompetitionProcessService
                .getAllMessagesForSession(session.id)
                .mapNotNull { it.body as? CompetitionStageChangedMessage }
                .dropWhile { it.to is CompetitionStage.WaitingStart }
                .firstOrNull()
        }

        @Suppress("unused")
        val expectedMessage = object {
            val from = CompetitionStage.WaitingStart(round = 1)
            val to = CompetitionStage.InProcess(round = 1)
            val roundLength = session.settings.roundLength
        }

        assertThat(generatedMessage)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .isEqualTo(expectedMessage)
        assertThat(generatedMessage?.timeStamp)
            .isNotNull
    }
}

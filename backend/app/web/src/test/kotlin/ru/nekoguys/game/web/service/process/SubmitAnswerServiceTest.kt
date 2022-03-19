package ru.nekoguys.game.web.service.process

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
import ru.nekoguys.game.entity.competition.rule.CompetitionAnswerSubmittedMessage
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.SubmitAnswerResponse
import ru.nekoguys.game.web.service.CompetitionProcessService
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.util.concurrent.TimeUnit
import ru.nekoguys.game.entity.competition.CompetitionProcessService as CoreCompetitionProcessService

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class SubmitAnswerServiceTest @Autowired constructor(
    private val competitionProcessService: CompetitionProcessService,
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val game: TestGame,
) {

    @Test
    fun `answer is saved when captain submits answer`() {
        val student = game.createUser(UserRole.Student)
        val session = game.createAndLoadSession { pin ->
            game.createTeam(pin)
            game.createTeam(pin, captain = student)
            game.startCompetition(pin)
            game.startRound(pin)
        }
        val roundNumber = (session.stage as CompetitionStage.InProcess).round

        val response = runBlocking {
            competitionProcessService.submitAnswer(
                studentEmail = student.email,
                sessionPin = session.pin,
                roundNumber = roundNumber,
                answer = 42,
            )
        }

        assertThat(response)
            .usingRecursiveComparison()
            .isEqualTo(SubmitAnswerResponse)

        val actualAnswer =
            game.loadSession(session.pin)
                .rounds
                .single { it.roundNumber == roundNumber }
                .answers
                .single()

        assertThat(actualAnswer.value)
            .isEqualTo(42)
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `event is sent when answer is submitted`() {
        val student = game.createUser(UserRole.Student)
        val session = game.createAndLoadSession { pin ->
            game.createTeam(pin, captain = student)
            game.createTeam(pin)
            game.startCompetition(pin)
            game.startRound(pin)
            game.submitAnswer(pin, student, 42)
        }

        val generatedMessage = runBlocking {
            // если что-то пойдёт не так при получении ивента,
            // тест может зависнуть намертво
            // поэтому в тесте проставлен таймаут
            coreCompetitionProcessService
                .getAllMessagesForSession(session.id)
                .mapNotNull { it.body as? CompetitionAnswerSubmittedMessage }
                .firstOrNull()
        }

        @Suppress("unused")
        val expectedMessage = object {
            val teamNumberInGame = 1
            val roundNumber = 1
            val answer = 42L
        }

        assertThat(generatedMessage)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .isEqualTo(expectedMessage)
    }
}

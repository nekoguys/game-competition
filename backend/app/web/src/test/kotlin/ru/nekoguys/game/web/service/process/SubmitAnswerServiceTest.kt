package ru.nekoguys.game.web.service.process

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.commongame.service.pin
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.rule.CompetitionAnswerSubmittedMessage
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.util.concurrent.TimeUnit
import ru.nekoguys.game.entity.competition.service.CompetitionProcessService as CoreCompetitionProcessService

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class SubmitAnswerServiceTest @Autowired constructor(
    private val coreCompetitionProcessService: CoreCompetitionProcessService,
    private val game: TestGame,
) {

    lateinit var teacher: User
    lateinit var student: User
    lateinit var session: CompetitionSession

    @BeforeEach
    fun createUsers() {
        teacher = game.createUser(UserRole.Teacher, TestGame.DEFAULT_TEACHER_EMAIL)
        student = game.createUser(UserRole.Student, TestGame.DEFAULT_STUDENT_EMAIL)
        session = game.createAndLoadSession(teacher = teacher) { pin ->
            game.createTeam(pin, student)
            game.startCompetition(pin)
            game.startRound(pin)
        }
    }

    @Test
    fun `answer is saved when captain submits answer`() {
        game.submitAnswer(session.pin, student, 42)

        val actualAnswer =
            game.loadSession(session.pin)
                .rounds
                .single { it.roundNumber == 1 }
                .answers
                .single()

        assertThat(actualAnswer.production)
            .isEqualTo(42)
    }

    @Test
    fun `answer is saved when captain submits answer twice`() {
        game.submitAnswer(session.pin, student, 12)
        game.submitAnswer(session.pin, student, 42)

        val actualAnswer =
            game.loadSession(session.pin)
                .rounds
                .single { it.roundNumber == 1 }
                .answers
                .single()

        assertThat(actualAnswer.production)
            .isEqualTo(42)
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `event is sent when answer is submitted`() {
        game.submitAnswer(session.pin, student, 42)

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
            val roundNumber = 1
            val answer = 42
        }

        assertThat(generatedMessage)
            .usingRecursiveComparison()
            .ignoringExpectedNullFields()
            .isEqualTo(expectedMessage)
    }
}

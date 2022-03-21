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
import ru.nekoguys.game.entity.competition.rule.CompetitionAnnouncementMessage
import ru.nekoguys.game.entity.competition.service.CompetitionProcessService
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.util.concurrent.TimeUnit

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class SendAnnouncementServiceTest @Autowired constructor(
    private val competitionProcessService: CompetitionProcessService,
    private val game: TestGame,
) {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `event is sent on new announcement`() {
        val session = game.createAndLoadSession { pin ->
            repeat(2) { game.createTeam(pin) }
        }

        val announcement = "Cake is a lie"
        game.sendAnnouncement(
            sessionPin = session.pin,
            announcement = announcement,
        )
        val generatedMessage = runBlocking {
            // если что-то пойдёт не так при получении ивента,
            // тест может зависнуть намертво
            // поэтому в тесте проставлен таймаут
            competitionProcessService
                .getAllMessagesForSession(session.id)
                .mapNotNull { it.body as? CompetitionAnnouncementMessage }
                .firstOrNull()
        }

        @Suppress("unused")
        val expectedMessage = object {
            val announcement = announcement
        }

        assertThat(generatedMessage)
            .usingRecursiveComparison()
            .isEqualTo(expectedMessage)
    }

}

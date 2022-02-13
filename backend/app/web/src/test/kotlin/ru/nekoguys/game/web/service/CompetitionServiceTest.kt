package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import java.time.LocalDateTime

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class CompetitionServiceTest @Autowired constructor(
    private val game: TestGame,
) {

    @Test
    fun `can create competition in draft state`(): Unit = runBlocking {
        val session = game.createAndLoadCompetition(
            request = TestGame.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST,
        )

        with(session) {
            assertThat(settings.name).isNotNull
            assertThat(lastModified).isBefore(LocalDateTime.now())
            assertThat(stage).isEqualTo(CompetitionStage.Draft)
            assertThat(teams).isEmpty()
        }
    }

    @Test
    fun `can create competition in registration state`(): Unit = runBlocking {
        val session = game.createAndLoadCompetition(
            request = TestGame.DEFAULT_CREATE_COMPETITION_REQUEST,
        )

        with(session) {
            assertThat(settings.name).isNotNull
            assertThat(lastModified).isBefore(LocalDateTime.now())
            assertThat(stage).isEqualTo(CompetitionStage.Registration)
            assertThat(teams).isEmpty()
        }
    }

    @Test
    fun `can create a team`(): Unit = runBlocking {
        val competitionPin = game.createCompetition()
        val captain = game.createUser(role = UserRole.Student)
        game.createTeam(captain = captain, competitionPin = competitionPin)

        val session = game.loadCompetitionSession(competitionPin)

        assertThat(session.teams).hasSize(1)
        assertThat(session.teams[0].captain.user).isEqualTo(captain)
    }
}

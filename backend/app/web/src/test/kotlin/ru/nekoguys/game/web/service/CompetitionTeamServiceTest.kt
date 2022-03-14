package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.TeamMemberUpdateNotification
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class CompetitionTeamServiceTest @Autowired constructor(
    private val game: TestGame,
    private val competitionTeamService: CompetitionTeamService,
) {
    @Test
    fun `valid my team members flow`() {
        val sessionPin = game.createSession()
        val captain = game.createUser(role = UserRole.Student)
        val sampleMember = game.createUser(role = UserRole.Student)

        val captain2 = game.createUser(role = UserRole.Student)
        game.createTeam(sessionPin = sessionPin, captain = captain2)
        val team = game.createTeam(sessionPin = sessionPin, captain = captain)
        game.joinTeam(sessionPin, team.teamName, sampleMember, team.password)

        fun checkValidFlow(flow: Flow<TeamMemberUpdateNotification>) {
            runBlocking {
                val flowResult = flow.take(2).toList()
                assertThat(flowResult[0].name).isEqualTo(captain.email)
                assertThat(flowResult[0].isCaptain).isTrue()
                assertThat(flowResult[1].name).isEqualTo(sampleMember.email)
            }
        }
        checkValidFlow(competitionTeamService.joinUserTeamEventsFlow(captain.email, sessionPin))
        checkValidFlow(competitionTeamService.joinUserTeamEventsFlow(sampleMember.email, sessionPin))
    }
}

package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.CreateTeamRequest
import ru.nekoguys.game.web.dto.JoinTeamRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import ru.nekoguys.game.web.util.TestGame.TestData.DEFAULT_CREATE_COMPETITION_REQUEST
import ru.nekoguys.game.web.util.TestGame.TestData.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class CompetitionControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    private lateinit var testUser: User

    @BeforeEach
    fun createUser() {
        testUser = game.createUser(email = "test@hse.ru")
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["TEACHER"])
    @Test
    fun `create competition in draft stage`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["TEACHER"])
    @Test
    fun `can create competition in draft state`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["TEACHER"])
    @Test
    fun `can create competition in registration state`() {
        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.pin").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can create a team`() {
        val competitionPin = game.createCompetition()

        val request = CreateTeamRequest(
            pin = competitionPin,
            teamName = "Test team",
            captainEmail = testUser.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/create_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same captain`() {
        val (competitionPin) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            pin = competitionPin,
            teamName = "Another test team",
            captainEmail = testUser.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/create_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same name`() {
        val (competitionPin, teamName) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            pin = competitionPin,
            teamName = teamName,
            captainEmail = game.createUser().email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/create_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["STUDENT"])
    @Test
    fun `can join a team`() {
        val (competitionPin, teamName, password) = game.createTeam()

        val request = JoinTeamRequest(
            competitionPin = competitionPin,
            teamName = teamName,
            password = password
        )

        webTestClient
            .post()
            .uri("/api/competitions/join_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentTeamName").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["STUDENT"])
    @Test
    fun `can't join non-existent team`() {
        val competitionPin = game.createCompetition()

        val request = JoinTeamRequest(
            competitionPin = competitionPin,
            teamName = "Try this",
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/join_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["STUDENT"])
    @Test
    fun `can't join same team twice`() {
        val (competitionPin, teamName, password) =
            game.createAndJoinTeam(teamMember = testUser)

        val request = JoinTeamRequest(
            competitionPin = competitionPin,
            teamName = teamName,
            password = password
        )

        webTestClient
            .post()
            .uri("/api/competitions/join_team")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

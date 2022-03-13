package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.CheckGamePinRequest
import ru.nekoguys.game.web.dto.CreateTeamRequest
import ru.nekoguys.game.web.dto.JoinTeamRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame
import ru.nekoguys.game.web.util.TestGame.TestData.DEFAULT_ADMIN_EMAIL
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
        testUser = game.createUser(email = TestGame.DEFAULT_ADMIN_EMAIL)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["TEACHER"])
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can check if competition exists`() {
        val competitionPin = game.createCompetition()

        val request = CheckGamePinRequest(competitionPin)

        webTestClient
            .post()
            .uri("/api/competitions/check_pin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.exists").isEqualTo(true)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can check if competition doesn't exists`() {
        val competitionPin = game.createCompetition()

        val request = CheckGamePinRequest(
            pin = "123$competitionPin",
        )

        webTestClient
            .post()
            .uri("/api/competitions/check_pin")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.exists").isEqualTo(false)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can create a team`() {
        val competitionPin = game.createCompetition()

        val request = CreateTeamRequest(
            gameId = competitionPin,
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same captain`() {
        val (competitionPin) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            gameId = competitionPin,
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same name`() {
        val (competitionPin, teamName) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            gameId = competitionPin,
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create too many teams`() {
        val competitionPin = game.createCompetition(
            request = DEFAULT_CREATE_COMPETITION_REQUEST.copy(
                maxTeamsAmount = 2,
            )
        )
        repeat(2) { game.createTeam(competitionPin) }

        val request = CreateTeamRequest(
            gameId = competitionPin,
            teamName = "Test team name",
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
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

    @WithMockUser(username = DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
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

    @WithMockUser(username = DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
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

    @WithMockUser(username = DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't join full team`() {
        val competitionPin = game.createCompetition(
            request = DEFAULT_CREATE_COMPETITION_REQUEST.copy(
                maxTeamSize = 2,
            )
        )
        val (_, teamName, password) = game.createAndJoinTeam(competitionPin)
        game.joinTeam(competitionPin, teamName)
        game.joinTeam(competitionPin, teamName)

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

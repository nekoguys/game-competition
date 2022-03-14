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

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class CompetitionTeamControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    private lateinit var testUser: User

    @BeforeEach
    fun createUser() {
        testUser = game.createUser(email = TestGame.DEFAULT_ADMIN_EMAIL)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can create a team`() {
        val sessionPin = game.createSession()

        val request = CreateTeamRequest(
            teamName = "Test team",
            captainEmail = testUser.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/create")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same captain`() {
        val (sessionPin) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            teamName = "Another test team",
            captainEmail = testUser.email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/create")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create a team with same name`() {
        val (sessionPin, teamName) = game.createTeam(captain = testUser)

        val request = CreateTeamRequest(
            teamName = teamName,
            captainEmail = game.createUser().email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/create")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't create too many teams`() {
        val sessionPin = game.createSession(
            request = DEFAULT_CREATE_COMPETITION_REQUEST.copy(
                maxTeamsAmount = 2,
            )
        )
        repeat(2) { game.createTeam(sessionPin) }

        val request = CreateTeamRequest(
            teamName = "Test team name",
            captainEmail = game.createUser().email,
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/create")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can join a team`() {
        val (sessionPin, teamName, password) = game.createTeam()

        val request = JoinTeamRequest(
            teamName = teamName,
            password = password
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/join")
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.currentTeamName").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't join non-existent team`() {
        val sessionPin = game.createSession()

        val request = JoinTeamRequest(
            teamName = "Try this",
            password = TestGame.DEFAULT_PASSWORD,
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/join")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't join same team twice`() {
        val (sessionPin, teamName, password) =
            game.createAndJoinTeam(teamMember = testUser)

        val request = JoinTeamRequest(
            teamName = teamName,
            password = password
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/join")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't join team using incorrect password`() {
        val (sessionPin, teamName, password) =
            game.createAndJoinTeam(teamMember = testUser)

        val request = JoinTeamRequest(
            teamName = teamName,
            password = "wrong $password"
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/join")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can't join full team`() {
        val sessionPin = game.createSession(
            request = DEFAULT_CREATE_COMPETITION_REQUEST.copy(
                maxTeamSize = 2,
            )
        )
        val (_, teamName, password) = game.createAndJoinTeam(sessionPin)
        game.joinTeam(sessionPin, teamName)
        game.joinTeam(sessionPin, teamName)

        val request = JoinTeamRequest(
            teamName = teamName,
            password = password
        )

        webTestClient
            .post()
            .uri("/api/competitions/$sessionPin/teams/join")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `captain can view current team info`() {
        val (sessionPin, teamName, password) =
            game.createTeam(captain = testUser)

        webTestClient
            .get()
            .uri("/api/competitions/$sessionPin/teams/current")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.teamName").isEqualTo(teamName)
            .jsonPath("$.password").isEqualTo(password)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `team member can view current team info`() {
        val (sessionPin, teamName, _) =
            game.createAndJoinTeam(teamMember = testUser)

        webTestClient
            .get()
            .uri("/api/competitions/$sessionPin/teams/current")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.teamName").isEqualTo(teamName)
            .jsonPath("$.password").doesNotExist()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `not registered player can't view current team info`() {
        val sessionPin = game.createSession()

        webTestClient
            .get()
            .uri("/api/competitions/$sessionPin/teams/current")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").exists()
    }
}

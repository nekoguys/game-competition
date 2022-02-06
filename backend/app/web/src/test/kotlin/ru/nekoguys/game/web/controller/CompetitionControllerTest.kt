package ru.nekoguys.game.web.controller

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.CreateTeamRequest
import ru.nekoguys.game.web.dto.JoinTeamRequest
import ru.nekoguys.game.web.util.TestUtils
import ru.nekoguys.game.web.util.TestUtils.TestData.DEFAULT_CREATE_COMPETITION_REQUEST
import ru.nekoguys.game.web.util.TestUtils.TestData.DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST

@GameWebApplicationIntegrationTest
class CompetitionControllerTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val webTestClient: WebTestClient,
    private val testUtils: TestUtils,
) {

    @WithMockUser(username = "test@hse.ru", roles = ["TEACHER"])
    @Test
    fun `create competition in draft stage`(): Unit = runBlocking {
        testUtils.createUser(email = "test@hse.ru")

        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("message").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["TEACHER"])
    @Test
    fun `can create competition in draft state`(): Unit = runBlocking {
        testUtils.createUser(email = "test@hse.ru")

        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_DRAFT_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["TEACHER"])
    @Test
    fun `can create competition in registration state`(): Unit = runBlocking {
        testUtils.createUser(email = "test@hse.ru")

        webTestClient
            .post()
            .uri("/api/competitions/create")
            .bodyValue(DEFAULT_CREATE_COMPETITION_REQUEST)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.pin").exists()
    }

    @WithMockUser(username = "test@hse.ru", roles = ["STUDENT"])
    @Test
    fun `can create a team`(): Unit = runBlocking {
        val student = testUtils.createUser(email = "test@hse.ru")
        val competitionPin = testUtils.createCompetition()

        val request = CreateTeamRequest(
            pin = competitionPin,
            teamName = "Test team",
            captainEmail = student.email,
            password = "password"
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

    @WithMockUser(username = "test@hse.ru", roles = ["STUDENT"])
    @Test
    fun `can't create a team twice`(): Unit = runBlocking {
        val student = testUtils.createUser(email = "test@hse.ru")
        val (competitionPin) = testUtils.createTeam(captain = student)

        val request = CreateTeamRequest(
            pin = competitionPin,
            teamName = "Another test team",
            captainEmail = student.email,
            password = "password"
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
    fun `can join a team`(): Unit = runBlocking {
        testUtils.createUser(email = "test@hse.ru")
        val (competitionPin, teamName, password) = testUtils.createTeam()

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
    fun `can't join same team twice`(): Unit = runBlocking {
        val student = testUtils.createUser(email = "test@hse.ru")
        val (competitionPin, teamName, password) = testUtils.joinTeam(teamMate = student)

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

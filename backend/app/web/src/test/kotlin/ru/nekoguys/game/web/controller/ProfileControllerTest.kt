package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.ProfileUpdateRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class ProfileControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    private lateinit var testUser: User

    @BeforeEach
    fun createUser() {
        testUser = game.createUser(email = TestGame.DEFAULT_EMAIL)
    }

    @WithMockUser(username = TestGame.DEFAULT_EMAIL, roles = ["TEACHER"])
    @Test
    fun `update profile`() {
        webTestClient
            .post()
            .uri("/api/profile/update")
            .bodyValue(
                ProfileUpdateRequest(
                    name = "Triss",
                    surname = "Merigold",
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath(
                "$..['email', 'role', 'name', 'surname', 'userDescription']"
            ).exists()
    }
}

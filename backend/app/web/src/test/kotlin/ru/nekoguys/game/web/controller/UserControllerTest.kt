package ru.nekoguys.game.web.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.WebTestClient
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.web.GameWebApplicationIntegrationTest
import ru.nekoguys.game.web.dto.UserUpdateRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationIntegrationTest
class UserControllerTest @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val game: TestGame,
) {

    private lateinit var testStudent: User
    private lateinit var testAdmin: User

    @BeforeEach
    fun createUser() {
        testStudent = game.createUser(email = TestGame.DEFAULT_STUDENT_EMAIL, role = UserRole.Student)
        testAdmin = game.createUser(email = TestGame.DEFAULT_ADMIN_EMAIL)
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `update profile`() {
        webTestClient
            .post()
            .uri("/api/user/update")
            .bodyValue(
                UserUpdateRequest(
                    email = null,
                    role = null,
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT", "TEACHER", "ADMIN"])
    @Test
    fun `update user role`() {
        val userForUpdate = game.createUser(
            role = UserRole.Student
        )

        webTestClient
            .post()
            .uri("/api/user/update_role")
            .bodyValue(
                UserUpdateRequest(
                    email = userForUpdate.email,
                    role = UserRole.Teacher.toString(),
                    name = null,
                    surname = null,
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath(
                "$..['role']"
            ).isEqualTo(UserRole.Teacher.toString())
    }

    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"]) // why he is admin? (testUser moment)
    @Test
    fun `student can't update another user`() {
        val userForUpdate = game.createUser(
            role = UserRole.Student
        )
        webTestClient
            .post()
            .uri("/api/user/update_role")
            .bodyValue(
                UserUpdateRequest(
                    email = userForUpdate.email,
                    role = UserRole.Teacher.toString(),
                    name = null,
                    surname = null,
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"]) // why he is admin? (testUser moment)
    @Test
    fun `user can't update himself`() {
        webTestClient
            .post()
            .uri("/api/user/update_role")
            .bodyValue(
                UserUpdateRequest(
                    email = TestGame.DEFAULT_ADMIN_EMAIL,
                    role = UserRole.Teacher.toString(),
                    name = null,
                    surname = null,
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isForbidden
    }
}

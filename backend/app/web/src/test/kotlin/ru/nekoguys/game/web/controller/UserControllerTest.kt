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
import ru.nekoguys.game.web.dto.FindUserByEmailRequest
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
        testStudent = game.createUser(
            role = UserRole.Student,
            email = TestGame.DEFAULT_STUDENT_EMAIL,
        )
        testAdmin = game.createUser(
            role = UserRole.Admin,
            email = TestGame.DEFAULT_ADMIN_EMAIL
        )
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can get current user`() {
        webTestClient
            .get()
            .uri("/api/users")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo(testAdmin.email)
            .jsonPath("$..['role', 'name', 'surname', 'userDescription']").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    @Test
    fun `student can't get other user`() {
        webTestClient
            .post()
            .uri("/api/users/find_by_email")
            .bodyValue(
                FindUserByEmailRequest(email = testAdmin.email)
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `admin can get other user`() {
        webTestClient
            .post()
            .uri("/api/users/find_by_email")
            .bodyValue(
                FindUserByEmailRequest(email = testStudent.email)
            )
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.email").isEqualTo(testStudent.email)
            .jsonPath("$..['role', 'name', 'surname', 'userDescription']").exists()
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can update user`() {
        webTestClient
            .post()
            .uri("/api/users/update")
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

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `can update user role`() {
        val userForUpdate = game.createUser(
            role = UserRole.Student
        )

        webTestClient
            .post()
            .uri("/api/users/update")
            .bodyValue(
                UserUpdateRequest(
                    email = userForUpdate.email,
                    role = UserRole.Teacher.topRoleName,
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

    @WithMockUser(username = TestGame.DEFAULT_STUDENT_EMAIL, roles = ["STUDENT"])
    @Test
    fun `student can't update another user`() {
        val userForUpdate = game.createUser(
            role = UserRole.Student
        )
        webTestClient
            .post()
            .uri("/api/users/update")
            .bodyValue(
                UserUpdateRequest(
                    email = userForUpdate.email,
                    role = UserRole.Teacher.topRoleName,
                    name = null,
                    surname = null,
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isForbidden
    }

    @WithMockUser(username = TestGame.DEFAULT_ADMIN_EMAIL, roles = ["STUDENT"])
    @Test
    fun `user can't update himself`() {
        webTestClient
            .post()
            .uri("/api/users/update")
            .bodyValue(
                UserUpdateRequest(
                    email = TestGame.DEFAULT_ADMIN_EMAIL,
                    role = UserRole.Teacher.topRoleName,
                    name = null,
                    surname = null,
                    newPassword = null,
                )
            )
            .exchange()
            .expectStatus().isForbidden
    }
}

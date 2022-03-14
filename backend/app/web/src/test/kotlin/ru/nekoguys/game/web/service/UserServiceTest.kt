package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.model.UserRole.Teacher
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.UserUpdateRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class UserServiceTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val testGame: TestGame,
) {

    @Test
    fun `can update user`() {
        val user = testGame.createUser()
        val userUpdateRequest = UserUpdateRequest(
            name = "Triss",
            surname = "Merigold",
            newPassword = null,
            role = null,
            email = null,
        )

        runBlocking {
            userService.updateUser(
                operatorEmail = user.email,
                request = userUpdateRequest,
            )
        }

        val profile = runBlocking { userRepository.findByEmail(user.email) }

        with(userUpdateRequest) {
            assertThat(profile)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(@Suppress("unused") object {
                    val role = Teacher
                    val firstName = name
                    val secondName = surname
                })
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["asya", "вася пупкин", "кин вас"])
    fun `can search user`(query: String) {
        val searchUser = testGame.createUser(
            email = "vasya@edu.hse.ru",
            firstName = "Вася",
            secondName = "Пупкин",
            role = UserRole.Admin,
        )

        val users = runBlocking {
            userService.findUsers(
                query = query,
                page = 0,
                pageSize = 100,
            )
        }

        assertThat(users.results.size)
            .isEqualTo(1)
        assertThat(users.results.first())
            .usingRecursiveComparison()
            .isEqualTo(@Suppress("unused") object {
                val email = searchUser.email
                val role = searchUser.role.toString()
            })
    }
}

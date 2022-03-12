package ru.nekoguys.game.web.service

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.GameWebApplicationTest
import ru.nekoguys.game.web.dto.ProfileUpdateRequest
import ru.nekoguys.game.web.util.CleanDatabaseExtension
import ru.nekoguys.game.web.util.TestGame

@ExtendWith(CleanDatabaseExtension::class)
@GameWebApplicationTest
class ProfileServiceTest @Autowired constructor(
    private val profileService: ProfileService,
    private val userRepository: UserRepository,
    private val testGame: TestGame,
) {

    @Test
    fun `can update profile`() {
        val user = testGame.createUser()
        val profileUpdateRequest = ProfileUpdateRequest(
            name = "Triss",
            surname = "Merigold",
            newPassword = null,
        )

        runBlocking {
            profileService.updateProfile(
                email = user.email,
                profileUpdateRequest = profileUpdateRequest,
            )
        }

        val profile = runBlocking { userRepository.findByEmail(user.email) }

        with(profileUpdateRequest) {
            assertThat(profile)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(@Suppress("unused") object {
                    val role = UserRole.Teacher
                    val firstName = name
                    val secondName = surname
                })
        }
    }
}

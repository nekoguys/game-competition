package ru.nekoguys.game.web.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.ProfileResponse
import ru.nekoguys.game.web.dto.ProfileUpdateRequest

@Service
class ProfileService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun getProfile(
        email: String,
    ): ProfileResponse? {
        val user = userRepository.findByEmail(email)

        return user?.toProfileResponse() ?: throw return null
    }

    suspend fun updateProfile(
        email: String,
        profileUpdateRequest: ProfileUpdateRequest,
    ): ProfileResponse? {
        val user = userRepository.findByEmail(email) ?: return null
        val newUser = with(profileUpdateRequest) {
            user.copy(
                firstName = name ?: user.firstName,
                secondName = surname ?: user.secondName,
                password = newPassword
                    ?.let { passwordEncoder.encode(it) }
                    ?: user.password,
            )
        }
        userRepository.updateUser(newUser)
        return newUser.toProfileResponse()
    }
}

fun User.toProfileResponse(): ProfileResponse =
    ProfileResponse(
        email = email,
        role = role.toString(),
        name = firstName,
        surname = secondName,
        userDescription = if (firstName != null && secondName != null) {
            "$secondName ${firstName!![0]}."
        } else {
            email
        }
    )

package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.toList
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.model.toUserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.UserResponse
import ru.nekoguys.game.web.dto.UserSearchRequest
import ru.nekoguys.game.web.dto.UserSearchResponse
import ru.nekoguys.game.web.dto.UserUpdateRequest

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun getUser(
        email: String,
    ): UserResponse? {
        val user = userRepository.findByEmail(email)
        return user?.toUserResponse() ?: throw return null
    }

    suspend fun updateUser(
        operatorEmail: String,
        userUpdateRequest: UserUpdateRequest,
    ): UserResponse? {
        with(userUpdateRequest) {
            val operator = userRepository.findByEmail(operatorEmail)
                ?: error("Operator $operatorEmail not found")

            val targetUser = if (email != null)
                userRepository.findByEmail(email) ?: return null
            else operator

            if (operator.role != UserRole.Admin
                && operator.email != targetUser.email
            ) return UserResponse.ChangeAnotherUserForbidden(operator.email)

            if (operator.email == targetUser.email
                && (role != null && role.toUserRole() != operator.role)
            ) return UserResponse.ChangeSelfRoleForbidden(operator.email)

            val newUser = with(userUpdateRequest) {
                targetUser.copy(
                    role = role?.toUserRole() ?: targetUser.role,
                    firstName = name ?: targetUser.firstName,
                    secondName = surname ?: targetUser.secondName,
                    password = newPassword
                        ?.let { passwordEncoder.encode(it) }
                        ?: targetUser.password,
                )
            }
            userRepository.updateUser(newUser)
            return newUser.toUserResponse()
        }
    }

    suspend fun findUsers(
        userSearchRequest: UserSearchRequest,
    ): UserSearchResponse {
        val users = userRepository.searchUser(
            query = userSearchRequest.query,
            limit = userSearchRequest.pageSize,
            offset = userSearchRequest.page * userSearchRequest.pageSize,
        ).toList()
        return users.toUserSearchResponse()
    }
}

fun User.toUserResponse(): UserResponse =
    UserResponse.Success(
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

fun List<User>.toUserSearchResponse(): UserSearchResponse =
    UserSearchResponse(
        this.map {
            UserSearchResponse.Info(
                email = it.email,
                role = it.role.toString(),
            )
        }
    )

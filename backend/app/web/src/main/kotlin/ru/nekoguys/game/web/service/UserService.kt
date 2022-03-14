package ru.nekoguys.game.web.service

import kotlinx.coroutines.flow.toList
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.user.model.User
import ru.nekoguys.game.entity.user.model.UserRole
import ru.nekoguys.game.entity.user.model.toUserRole
import ru.nekoguys.game.entity.user.repository.UserRepository
import ru.nekoguys.game.web.dto.UserApiResponse
import ru.nekoguys.game.web.dto.UserResponse
import ru.nekoguys.game.web.dto.UserSearchResponse
import ru.nekoguys.game.web.dto.UserUpdateRequest

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    suspend fun getUser(
        operatorEmail: String,
        targetEmail: String,
    ): UserApiResponse<UserResponse>? {
        val operator = userRepository
            .findByEmail(operatorEmail)
            ?: return UserApiResponse.UserNotFound(operatorEmail)

        if (operator.role !is UserRole.Admin
            && operatorEmail != targetEmail
        ) {
            return UserApiResponse.AccessForbidden(
                operatorEmail = operatorEmail,
                targetEmail = targetEmail,
            )
        }

        return userRepository
            .findByEmail(targetEmail)
            ?.toUserResponse()
    }

    suspend fun updateUser(
        operatorEmail: String,
        request: UserUpdateRequest,
    ): UserApiResponse<UserResponse>? {
        val operator = userRepository.findByEmail(operatorEmail)
            ?: return UserApiResponse.UserNotFound(operatorEmail)

        val targetUser = if (request.email != null) {
            userRepository
                .findByEmail(request.email)
                ?: return UserApiResponse.UserNotFound(operatorEmail)
        } else {
            operator
        }

        if (operator.role !is UserRole.Admin
            && operator.email != targetUser.email
        ) {
            return UserApiResponse.AccessForbidden(
                operatorEmail = operator.email,
                targetEmail = targetUser.email,
            )
        }

        val requestedRole = request.role?.toUserRole()
        if (operator.email == targetUser.email
            && (requestedRole != null && requestedRole != operator.role)
        ) {
            return UserResponse.ChangeSelfRoleForbidden(operator.email)
        }

        val newUser = with(request) {
            targetUser.copy(
                role = requestedRole ?: targetUser.role,
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

    suspend fun findUsers(
        query: String,
        page: Int,
        pageSize: Int,
    ): UserSearchResponse =
        userRepository
            .searchUser(
                query = query,
                limit = pageSize,
                offset = page * pageSize,
            )
            .toList()
            .toUserSearchResponse()
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

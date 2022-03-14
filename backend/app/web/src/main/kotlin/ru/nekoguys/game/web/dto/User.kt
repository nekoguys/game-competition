package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

sealed class UserApiResponse<out T : UserApiResponse<T>>(
    status: HttpStatus,
) : WebResponse(status) {
    data class UserNotFound(
        val email: String
    ) : UserApiResponse<Nothing>(HttpStatus.BAD_REQUEST)

    data class AccessForbidden(
        val operatorEmail: String,
        val targetEmail: String,
    ) : UserApiResponse<Nothing>(HttpStatus.FORBIDDEN) {
        val message = "User '$operatorEmail' attempted to access user '$targetEmail', " +
                "it is forbidden"
    }
}

data class UserUpdateRequest(
    val email: String?,
    val role: String?,
    val name: String?,
    val surname: String?,
    val newPassword: String?,
)

sealed class UserResponse(
    status: HttpStatus,
) : UserApiResponse<UserResponse>(status) {

    data class Success(
        val email: String,
        val role: String,
        val name: String?,
        val surname: String?,
        val userDescription: String,
    ) : UserResponse(HttpStatus.OK)

    class ChangeSelfRoleForbidden(
        val email: String,
    ) : UserResponse(HttpStatus.FORBIDDEN) {
        val message = "User $email attempted to change his role himself"
    }
}

data class FindUserByEmailRequest(
    val email: String,
)

data class FindUsersByFilterRequest(
    val query: String,
    val page: Int,
    val pageSize: Int,
)

class UserSearchResponse(
    val results: List<Info>,
) : UserApiResponse<UserSearchResponse>(HttpStatus.OK) {
    data class Info(
        val email: String,
        val role: String,
    )
}

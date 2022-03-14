package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

data class UserUpdateRequest(
    val email: String?,
    val role: String?,
    val name: String?,
    val surname: String?,
    val newPassword: String?,
)

sealed class UserResponse(
    status: HttpStatus,
) : WebResponse(status) {

    data class Success(
        val email: String,
        val role: String,
        val name: String?,
        val surname: String?,
        val userDescription: String,
    ) : UserResponse(HttpStatus.OK)

    data class ChangeAnotherUserForbidden(
        val email: String,
    ) : UserResponse(HttpStatus.FORBIDDEN) {
        val message = "User $email attempted to edit another user, but he is not Admin"
    }

    class ChangeSelfRoleForbidden(
        val email: String,
    ) : UserResponse(HttpStatus.FORBIDDEN) {
        val message = "User $email attempted to change his role himself"
    }
}

data class UserSearchRequest(
    val query: String,
    val page: Int,
    val pageSize: Int,
)

class UserSearchResponse(
    val results: List<Info>,
) : WebResponse(HttpStatus.OK) {
    data class Info(
        val email: String,
        val role: String,
    )
}

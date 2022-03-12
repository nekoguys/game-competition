package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

data class ProfileResponse(
    val email: String,
    val role: String,
    val name: String?,
    val surname: String?,
    val userDescription: String,
) : WebResponse(HttpStatus.OK)

data class ProfileUpdateRequest(
    val name: String?,
    val surname: String?,
    val newPassword: String?,
)

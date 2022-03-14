@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

sealed class TeamApiResponse<out T : TeamApiResponse<T>>(
    status: HttpStatus
) : WebResponse(status) {

    class SessionNotFound(
        sessionPin: String,
    ) : TeamApiResponse<Nothing>(HttpStatus.NOT_FOUND) {
        val message = "There is no competition session with pin '$sessionPin'"
    }

    class UserIsNotRegistered(
        email: String,
    ) : TeamApiResponse<Nothing>(HttpStatus.BAD_REQUEST) {
        val message = "User with email '$email' is not registered"
    }

    class ProcessError(
        val message: String
    ) : TeamApiResponse<Nothing>(HttpStatus.BAD_REQUEST)
}

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
data class CreateTeamRequest(
    @JsonProperty("team_name")
    val teamName: String,
    @JsonProperty("captain_email")
    val captainEmail: String,
    val password: String,
)

sealed class CreateTeamResponse(
    status: HttpStatus,
) : TeamApiResponse<CreateTeamResponse>(status) {

    object Success : CreateTeamResponse(HttpStatus.OK) {
        @Suppress("unused")
        val message = "Team created successfully"
    }

    object IncorrectName : CreateTeamResponse(HttpStatus.BAD_REQUEST) {
        @Suppress("unused")
        val message = "Team name is empty or too small"
    }
}

data class JoinTeamRequest(
    val teamName: String,
    val password: String,
)

sealed class JoinTeamResponse(
    status: HttpStatus,
) : TeamApiResponse<JoinTeamResponse>(status) {

    data class Success(
        val currentTeamName: String,
    ) : JoinTeamResponse(HttpStatus.OK)
}

data class GetTeamResponse(
    val teamName: String,
    val password: String?,
) : TeamApiResponse<GetTeamResponse>(HttpStatus.OK)

data class TeamUpdateNotification(
    val teamName: String,
    val idInGame: Int,
    val teamMembers: List<String>,
)

data class TeamMemberUpdateNotification(
    val name: String,
    val isCaptain: Boolean
)

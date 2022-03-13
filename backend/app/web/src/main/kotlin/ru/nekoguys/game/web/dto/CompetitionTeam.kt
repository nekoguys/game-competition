@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

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
) : WebResponse(status) {

    object Success : CreateTeamResponse(HttpStatus.OK) {
        @Suppress("unused")
        val message = "Team created successfully"
    }

    class GameNotFound(
        sessionPin: String,
    ) : CreateTeamResponse(HttpStatus.BAD_REQUEST) {
        @Suppress("unused")
        val message = "Game with pin $sessionPin not found"
    }

    object IncorrectName : CreateTeamResponse(HttpStatus.BAD_REQUEST) {
        @Suppress("unused")
        val message = "Team name is empty or too small"
    }

    class ProcessError(
        @Suppress("unused")
        val message: String,
    ) : CreateTeamResponse(HttpStatus.BAD_REQUEST)
}

data class JoinTeamRequest(
    val teamName: String,
    val password: String,
)

sealed class JoinTeamResponse(
    status: HttpStatus,
) : WebResponse(status) {

    data class Success(
        val currentTeamName: String,
    ) : JoinTeamResponse(HttpStatus.OK)

    class GameNotFound(
        sessionPin: String,
    ) : JoinTeamResponse(HttpStatus.BAD_REQUEST) {
        @Suppress("unused")
        val message = "No competition with pin: $sessionPin"
    }

    data class ProcessError(
        val message: String,
    ) : JoinTeamResponse(HttpStatus.BAD_REQUEST)
}

data class TeamUpdateNotification(
    val teamName: String,
    val idInGame: Int,
    val teamMembers: List<String>,
)

package ru.nekoguys.game.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateCompetitionRequest(
    @NotNull
    @Size(min = 2, max = 2, message = "Demand formula should contain 2 values separated by ;")
    @JsonSerialize(contentUsing = ToStringSerializer::class)
    val demandFormula: List<Double>,

    @NotNull
    @Size(min = 3, max = 3, message = "Expenses formula should contain 3 values separated by ;")
    @JsonSerialize(contentUsing = ToStringSerializer::class)
    val expensesFormula: List<Double>,

    val instruction: String? = null,
    val isAutoRoundEnding: Boolean? = null,
    val maxTeamSize: Int? = null,
    val maxTeamsAmount: Int? = null,
    val name: String,
    val roundLength: Int? = null,
    val roundsCount: Int? = null,
    val shouldEndRoundBeforeAllAnswered: Boolean? = null,
    val shouldShowResultTableInEnd: Boolean? = null,
    val shouldShowStudentPreviousRoundResults: Boolean? = null,
    val showOtherTeamsMembers: Boolean? = null,
    val state: String? = null,
    val teamLossUpperbound: Int? = null,
)

sealed interface CreateCompetitionResponse {
    object Created : CreateCompetitionResponse {
        @Suppress("MayBeConstant")
        val message = "Competition created successfully"
    }

    data class OpenedRegistration(
        val pin: String,
    ) : CreateCompetitionResponse
}


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetCompetitionResponse(
    val demandFormula: String,
    val expensesFormula: String,
    val instruction: String,
    val isAutoRoundEnding: Boolean,
    val isOwned: Boolean,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    val lastUpdateTime: LocalDateTime,
    val maxTeamSize: Int,
    val maxTeamsAmount: Int,
    val name: String,
    val pin: String,
    val roundLength: Int,
    val roundsCount: Int,
    val shouldEndRoundBeforeAllAnswered: Boolean,
    val shouldShowResultTableInEnd: Boolean,
    val shouldShowStudentPreviousRoundResults: Boolean,
    val showOtherTeamsMembers: Boolean,
    val state: String,
    val teamLossUpperbound: Double,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CompetitionCloneInfoResponse(
    val name: String,
    val demandFormula: String,
    val expensesFormula: String,
    val maxTeamsAmount: Int,
    val maxTeamSize: Int,
    val roundsCount: Int,
    val roundLength: Int,
    val teamLossUpperbound: Double,
    val instruction: String,
    val shouldShowResultsTableInEnd: Boolean,
    val shouldShowStudentPreviousRoundResults: Boolean,
    val isAutoRoundEnding: Boolean,
    val showOtherTeamsMembers: Boolean
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreateTeamRequest(
    @JsonProperty("game_id")
    val pin: String,
    val teamName: String,
    val captainEmail: String,
    val password: String,
)

sealed interface CreateTeamResponse {
    object Success : CreateTeamResponse {
        @Suppress("MayBeConstant")
        val message = "Team created successfully"
    }

    class GameNotFound(
        sessionPin: String,
    ) : CreateTeamResponse {
        val message = "Game with pin $sessionPin not found"
    }

    object IncorrectName : CreateTeamResponse {
        @Suppress("MayBeConstant")
        val message = "Team name is empty or too small"
    }

    class ProcessError(val message: String) : CreateTeamResponse
}

data class JoinTeamRequest(
    val competitionPin: String,
    val teamName: String,
    val password: String,
)

sealed interface JoinTeamResponse {
    data class Success(
        val currentTeamName: String,
    ) : JoinTeamResponse

    class GameNotFound(
        sessionPin: String,
    ) : JoinTeamResponse {
        val message = "No competition with pin: $sessionPin"
    }

    data class ProcessError(val message: String) : JoinTeamResponse
}

data class CheckGamePinRequest(
    val pin: String,
)

data class CheckGamePinResponse(
    val exists: Boolean,
)

data class TeamUpdateNotification(
    val teamName: String,
    val idInGame: Int,
    val teamMembers: List<String>,
)

/*
public class TeamCreationEventDto implements Serializable {
    private static final long serialVersionUID = -4502650950386932982L;

    private String teamName;

    private int idInGame;

    private List<String> teamMembers;
}

 */

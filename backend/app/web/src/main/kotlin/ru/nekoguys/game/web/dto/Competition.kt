package ru.nekoguys.game.web.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
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

    data class CreatedRegistered(
        val pin: String,
    ) : CreateCompetitionResponse
}


data class CompetitionInfo(
    val name: String,
    val state: String? = null,
    val expensesFormula: List<String>,
    val demandFormula: List<String>,
    val maxTeamsAmount: Int? = null,
    val maxTeamSize: Int? = null,
    val roundsCount: Int? = null,
    val roundLength: Int? = null,
    val teamLossUpperbound: Double? = null,
    val instruction: String? = null,
    val shouldShowStudentPreviousRoundResults: Boolean? = null,
    val shouldEndRoundBeforeAllAnswered: Boolean? = null,
    val shouldShowResultTableInEnd: Boolean? = null,
    val isAutoRoundEnding: Boolean? = null,
    val showOtherTeamsMembers: Boolean? = null,
)

class GetCompetitionHistoryResponse(
    sessions: List<CompetitionInfo>,
) : List<CompetitionInfo> by sessions

@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

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

sealed class CreateCompetitionResponse(
    status: HttpStatus,
) : WebResponse(status) {

    object Created : CreateCompetitionResponse(HttpStatus.OK) {
        @Suppress("unused")
        val message = "Competition created successfully"
    }

    data class OpenedRegistration(
        val pin: String,
    ) : CreateCompetitionResponse(HttpStatus.OK)
}

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
) : WebResponse(HttpStatus.OK)

data class CheckGamePinRequest(
    val pin: String,
)

data class CheckGamePinResponse(
    val exists: Boolean,
) : WebResponse(HttpStatus.OK)

data class CompetitionResultsResponse(
    val competitionName: String,
    val instruction: String,
    val prices: Map<Int, Double>,
    val income: Map<Int, Map<Int, Double>>,
    val produced: Map<Int, Map<Int, Int>>,
    val teams: List<TeamUpdateNotification>,
    val teamsOrderInDecreasingByTotalPrice: List<Int>,
    val messages: List<AnnouncementEvent>,
    val strategyHolders: Map<Int, StrategyDto>, // IStrategySubmissionService.StrategyHolder
) : WebResponse(HttpStatus.OK)

data class StrategyDto(
    val strategy: String,
)

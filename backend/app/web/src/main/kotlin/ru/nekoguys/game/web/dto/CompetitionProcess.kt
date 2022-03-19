@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

sealed class ProcessApiResponse<out T : ProcessApiResponse<T>>(
    status: HttpStatus,
) : WebResponse(status) {
    class SessionNotFound(
        sessionPin: String,
    ) : ProcessApiResponse<Nothing>(HttpStatus.NOT_FOUND) {
        val message = "There is no competition session with pin '$sessionPin'"
    }

    data class ProcessError(
        val message: String,
    ) : ProcessApiResponse<Nothing>(HttpStatus.BAD_REQUEST)
}

object StartCompetitionResponse : ProcessApiResponse<StartCompetitionResponse>(HttpStatus.OK) {
    @Suppress("unused")
    val message = "Competition started successfully"
}

object StartRoundResponse : ProcessApiResponse<StartRoundResponse>(HttpStatus.OK) {
    @Suppress("unused")
    val message = "Round has started successfully"
}

object EndRoundResponse : ProcessApiResponse<EndRoundResponse>(HttpStatus.OK) {
    @Suppress("unused")
    val message = "Round ended successfully"
}

data class ChangeRoundLengthRequest(
    val newRoundLength: Int,
)

object ChangeRoundLengthResponse : ProcessApiResponse<ChangeRoundLengthResponse>(HttpStatus.OK) {
    val message = "Round length changed successfully"
}

sealed class RoundEvent {
    val type: String = this::class.java.simpleName

    data class NewRound(
        val roundLength: Int,
        val beginTime: Long,
        val roundNumber: Int
    ) : RoundEvent()

    data class EndRound(
        val roundNumber: Int,
        val isEndOfGame: Boolean,
        val roundLength: Int,
    ) : RoundEvent()
}

data class CompetitionInfoForResultsTableResponse(
    val connectedTeamsCount: Int,
    val roundsCount: Int,
    val name: String,
    val isAutoRoundEnding: Boolean,
) : ProcessApiResponse<CompetitionInfoForResultsTableResponse>(HttpStatus.OK)

data class CompetitionInfoForStudentResultsTableResponse(
    val name: String,
    val description: String,
    val teamName: String,
    val teamIdInGame: Int,
    val shouldShowResultTable: Boolean,
    val shouldShowResultTableInEnd: Boolean,
    val isCaptain: Boolean,
    val roundsCount: Int,
    val strategy: String,
) : ProcessApiResponse<CompetitionInfoForResultsTableResponse>(HttpStatus.OK)

data class SubmitAnswerRequest(
    val answer: Int,
    val roundNumber: Int,
)

object SubmitAnswerResponse
    : ProcessApiResponse<SubmitAnswerResponse>(HttpStatus.OK) {
    @Suppress("unused")
    val message = "Answer submitted successfully"
}

data class SubmittedAnswerEvent(
    val teamIdInGame: Int,
    val roundNumber: Int,
    val teamAnswer: Int,
)

data class PriceChangeEvent(
    val roundNumber: Int,
    val price: Double,
)

data class RoundTeamResultEvent(
    val teamIdInGame: Int,
    val roundNumber: Int,
    val income: Double,
)

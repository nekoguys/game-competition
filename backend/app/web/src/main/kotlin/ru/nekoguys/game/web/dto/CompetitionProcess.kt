@file:Suppress("MayBeConstant")

package ru.nekoguys.game.web.dto

import org.springframework.http.HttpStatus
import ru.nekoguys.game.web.util.WebResponse

sealed class StartCompetitionResponse(
    status: HttpStatus
) : WebResponse(status) {
    object Success : StartCompetitionResponse(HttpStatus.OK) {
        @Suppress("unused")
        val message = "Competition started successfully"
    }

    data class ProcessError(
        val message: String,
    ) : StartCompetitionResponse(HttpStatus.BAD_REQUEST)
}

sealed class RoundEvent {
    val type: String = this::class.java.simpleName

    data class NewRound(
        val roundLength: Int,
        val beginTime: Long,
        val roundNumber: Int
    ): RoundEvent()

    data class EndRound(
        val roundNumber: Int,
        val isEndOfGame: Boolean,
        val roundLength: Int,
    ): RoundEvent()
}

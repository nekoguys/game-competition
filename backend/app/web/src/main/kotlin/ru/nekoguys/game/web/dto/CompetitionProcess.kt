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

/*
public class NewRoundEventDto implements ITypedEvent {

    private static final long serialVersionUID = 2297590174396281095L;

    @Builder.Default
    private String type = "NewRound";

    private int roundLength;

    private long beginTime;

    private int roundNumber;
}

public class EndRoundEventDto implements ITypedEvent {
    private static final long serialVersionUID = 6160337879050418193L;

    @Builder.Default
    private String type = "EndRound";

    private int roundNumber;

    private boolean isEndOfGame;

    private int roundLength;
}

 */

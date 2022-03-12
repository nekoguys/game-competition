package ru.nekoguys.game.core.util

import ru.nekoguys.game.core.DeferredCommandRequest
import ru.nekoguys.game.core.GameCommandRequest
import ru.nekoguys.game.core.GameMessage

@DslMarker
annotation class ResponseBuilderDsl

@ResponseBuilderDsl
class ResponseBuilder<P, Msg> {
    val responses = mutableListOf<GameMessage<P, Msg>>()

    inline operator fun P.invoke(
        block: ResponseToRecipientBuilder<P, Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToRecipientBuilder<P, Msg>(setOf(this))
                .apply(block)
                .responses
    }

    inline operator fun Collection<P>.invoke(
        block: ResponseToRecipientBuilder<P, Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToRecipientBuilder<P, Msg>(this)
                .apply(block)
                .responses
    }
}

fun <P, Cmd> ResponseBuilder<*, *>.defer(
    fromPlayer: P,
    command: Cmd,
    timeoutMillis: Long = 0,
) {
    deferCommandInternal(GameCommandRequest(fromPlayer, command), timeoutMillis)
}

fun <P, Cmd> ResponseBuilder<*, *>.defer(
    request: GameCommandRequest<P, Cmd>,
    timeoutMillis: Long = 0,
) {
    deferCommandInternal(request, timeoutMillis)
}

private fun <P, Cmd, P2, Msg> ResponseBuilder<P2, Msg>.deferCommandInternal(
    request: GameCommandRequest<P, Cmd>,
    timeoutMillis: Long = 0,
) {
    responses += DeferredCommandRequest(request, timeoutMillis)
}

@ResponseBuilderDsl
class ResponseToRecipientBuilder<P, Msg>(
    private val recipients: Collection<P>,
) {
    val responses = mutableListOf<GameMessage<P, Msg>>()

    operator fun Msg.unaryPlus() {
        add(this)
    }

    fun add(message: Msg) {
        if (recipients.isNotEmpty()) {
            responses += GameMessage(recipients.toSet(), message)
        }
    }
}

inline fun <P, Msg> buildResponse(
    block: ResponseBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseBuilder<P, Msg>()
        .apply(block)
        .responses

inline fun <P, Msg> buildResponse(
    recipient: P,
    block: ResponseToRecipientBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseToRecipientBuilder<P, Msg>(setOf(recipient))
        .apply(block)
        .responses

inline fun <P, Msg> buildResponse(
    recipients: Collection<P>,
    block: ResponseToRecipientBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseToRecipientBuilder<P, Msg>(recipients)
        .apply(block)
        .responses

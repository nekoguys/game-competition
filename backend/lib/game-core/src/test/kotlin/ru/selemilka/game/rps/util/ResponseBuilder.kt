package ru.selemilka.game.rps.util

import ru.selemilka.game.core.base.DeferredCommandRequest
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.rps.model.RpsPlayer

@DslMarker
annotation class RpsResponseDsl

@RpsResponseDsl
class ResponseBuilder<P : RpsPlayer, Msg> {
    val responses = mutableListOf<GameMessage<P, Msg>>()

    inline operator fun P.invoke(
        block: ResponseToPlayerBuilder<P, Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayerBuilder<P, Msg>(this)
                .apply(block)
                .responses
    }

    inline operator fun Collection<P>.invoke(
        block: ResponseToPlayersBuilder<P, Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayersBuilder<P, Msg>(this)
                .apply(block)
                .responses
    }

    fun <Cmd> deferCommand(
        fromPlayer: P,
        command: Cmd,
        timeoutMillis: Long = 0,
    ) {
        deferCommand(GameCommandRequest(fromPlayer, command), timeoutMillis)
    }

    fun <Cmd> deferCommand(
        request: GameCommandRequest<P, Cmd>,
        timeoutMillis: Long = 0,
    ) {
        responses += DeferredCommandRequest(request, timeoutMillis)
    }
}

@RpsResponseDsl
class ResponseToPlayerBuilder<P : RpsPlayer, Msg>(
    private val player: P,
) {
    val responses = mutableListOf<GameMessage<P, Msg>>()

    operator fun Msg.unaryPlus() {
        responses += GameMessage(player, this)
    }
}

@RpsResponseDsl
class ResponseToPlayersBuilder<P : RpsPlayer, Msg>(
    private val players: Collection<P>,
) {
    val responses = mutableListOf<GameMessage<P, Msg>>()

    operator fun Msg.unaryPlus() {
        responses += players.map { player -> GameMessage(player, this) }
    }
}

/**
 * Сообщения рассылаются:
 * * конкретному игроку [P]
 * * всем игрокам в сессии
 */
inline fun <P : RpsPlayer, Msg> buildResponse(
    block: ResponseBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseBuilder<P, Msg>()
        .apply(block)
        .responses

inline fun <P : RpsPlayer, Msg> buildResponse(
    player: P,
    block: ResponseToPlayerBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseToPlayerBuilder<P, Msg>(player)
        .apply(block)
        .responses

inline fun <P : RpsPlayer, Msg> buildResponse(
    players: Collection<P>,
    block: ResponseToPlayersBuilder<P, Msg>.() -> Unit,
): List<GameMessage<P, Msg>> =
    ResponseToPlayersBuilder<P, Msg>(players)
        .apply(block)
        .responses

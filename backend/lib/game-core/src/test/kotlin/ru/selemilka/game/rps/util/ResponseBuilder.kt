package ru.selemilka.game.rps.util

import ru.selemilka.game.core.base.DeferredCommandRequest
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsMessage

@DslMarker
annotation class RpsResponseDsl

@RpsResponseDsl
class ResponseBuilder<Msg : RpsMessage> {
    val responses = mutableListOf<GameMessage<RpsPlayer.Human, Msg>>()

    inline operator fun RpsPlayer.Human.invoke(
        block: ResponseToPlayerBuilder<Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayerBuilder<Msg>(this)
                .apply(block)
                .responses
    }

    inline operator fun Collection<RpsPlayer.Human>.invoke(
        block: ResponseToPlayersBuilder<Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayersBuilder<Msg>(this)
                .apply(block)
                .responses
    }
}

fun ResponseBuilder<*>.deferCommand(
    fromPlayer: RpsPlayer,
    command: RpsCommand,
    timeoutMillis: Long = 0,
) {
    deferCommandInternal(GameCommandRequest(fromPlayer, command), timeoutMillis)
}

fun ResponseBuilder<*>.deferCommand(
    request: GameCommandRequest<RpsPlayer, RpsCommand>,
    timeoutMillis: Long = 0,
) {
    deferCommandInternal(request, timeoutMillis)
}

/**
 * Такая реализация [deferCommand] не работает:
 * ```
 * fun <P : RpsPlayer, Cmd : RpsCommand> ResponseBuilder<*>.deferCommandWrong(
 *     request: GameCommandRequest<P, Cmd>,
 *     timeoutMillis: Long = 0,
 * ) {
 *     val inResponses: MutableList<GameMessage<RpsPlayer.Human, *>> = responses
 *     inResponses += DeferredCommandRequest(request, timeoutMillis)
 * }
 * ```
 *
 *
 */
private fun <T : RpsMessage, P : RpsPlayer, Cmd : RpsCommand> ResponseBuilder<T>.deferCommandInternal(
    request: GameCommandRequest<P, Cmd>,
    timeoutMillis: Long = 0,
) {
    val inResponses: MutableList<GameMessage<RpsPlayer.Human, T>> = responses
    inResponses += DeferredCommandRequest(request, timeoutMillis)
}


@RpsResponseDsl
class ResponseToPlayerBuilder<Msg : RpsMessage>(
    private val player: RpsPlayer.Human,
) {
    val responses = mutableListOf<GameMessage<RpsPlayer.Human, Msg>>()

    operator fun Msg.unaryPlus() {
        responses += GameMessage(player, this)
    }
}

@RpsResponseDsl
class ResponseToPlayersBuilder<Msg : RpsMessage>(
    private val players: Collection<RpsPlayer.Human>,
) {
    val responses = mutableListOf<GameMessage<RpsPlayer.Human, Msg>>()

    operator fun Msg.unaryPlus() {
        if (players.isNotEmpty()) {
            responses += GameMessage(players.toSet(), this)
        }
    }
}

inline fun <Msg : RpsMessage> buildResponse(
    block: ResponseBuilder<Msg>.() -> Unit,
): List<GameMessage<RpsPlayer.Human, Msg>> =
    ResponseBuilder<Msg>()
        .apply(block)
        .responses

inline fun <Msg : RpsMessage> buildResponse(
    player: RpsPlayer.Human,
    block: ResponseToPlayerBuilder<Msg>.() -> Unit,
): List<GameMessage<RpsPlayer.Human, Msg>> =
    ResponseToPlayerBuilder<Msg>(player)
        .apply(block)
        .responses

inline fun <Msg : RpsMessage> buildResponse(
    players: Collection<RpsPlayer.Human>,
    block: ResponseToPlayersBuilder<Msg>.() -> Unit,
): List<GameMessage<RpsPlayer.Human, Msg>> =
    ResponseToPlayersBuilder<Msg>(players)
        .apply(block)
        .responses

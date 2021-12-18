package ru.selemilka.game.rps.rules

import ru.selemilka.game.core.base.TargetedMessage
import ru.selemilka.game.rps.model.RpsPlayer

@DslMarker
annotation class ResponseDsl

@ResponseDsl
class ResponseBuilder<Msg> {
    val responses = mutableListOf<RpsResponse<Msg>>()

    infix fun Msg.sendTo(player: RpsPlayer) {
        responses += TargetedMessage(player, this)
    }

    infix fun Msg.sendTo(players: Collection<RpsPlayer>) {
        players.forEach { player -> responses += TargetedMessage(player, this) }
    }

    inline operator fun RpsPlayer.invoke(
        block: ResponseToPlayerBuilder<Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayerBuilder<Msg>(this)
                .apply(block)
                .responses
    }

    inline operator fun Collection<RpsPlayer>.invoke(
        block: ResponseToPlayersBuilder<Msg>.() -> Unit,
    ) {
        responses +=
            ResponseToPlayersBuilder<Msg>(this)
                .apply(block)
                .responses
    }
}

@ResponseDsl
class ResponseToPlayerBuilder<Msg>(
    private val player: RpsPlayer,
) {
    val responses = mutableListOf<RpsResponse<Msg>>()

    operator fun Msg.unaryPlus() {
        responses += TargetedMessage(player, this)
    }
}

@ResponseDsl
class ResponseToPlayersBuilder<Msg>(
    private val players: Collection<RpsPlayer>,
) {
    val responses = mutableListOf<RpsResponse<Msg>>()

    operator fun Msg.unaryPlus() {
        responses += players.map { player -> TargetedMessage(player, this) }
    }
}

/**
 * Сообщения рассылаются:
 * * конкретному игроку [RpsPlayer]
 * * всем игрокам в сессии
 */
inline fun <Msg> respond(
    block: ResponseBuilder<Msg>.() -> Unit,
): List<RpsResponse<Msg>> =
    ResponseBuilder<Msg>()
        .apply(block)
        .responses

inline fun <Msg> respond(
    player: RpsPlayer,
    block: ResponseToPlayerBuilder<Msg>.() -> Unit,
): List<RpsResponse<Msg>> =
    ResponseToPlayerBuilder<Msg>(player)
        .apply(block)
        .responses

inline fun <Msg> respond(
    players: Collection<RpsPlayer>,
    block: ResponseToPlayersBuilder<Msg>.() -> Unit,
): List<RpsResponse<Msg>> =
    ResponseToPlayersBuilder<Msg>(players)
        .apply(block)
        .responses

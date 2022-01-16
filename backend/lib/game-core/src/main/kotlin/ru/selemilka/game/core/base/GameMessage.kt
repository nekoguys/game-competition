package ru.selemilka.game.core.base

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * Сообщение, возвращаемое методом [GameRule.process] для взаимодействия с сессией
 *
 * Единственный способ создать свой объект GameMessage -
 * использовать фабричную функцию [GameMessage]
 */
@Serializable
sealed class GameMessage<out P, out T> {
    abstract val player: P
    abstract val body: T
}

/**
 * Сообщение, отправляемое игроку в игровой сессии
 *
 * [Msg] - Событие, отправляемое игровым сервером игрокам.
 * В игре "Конкуренция" наследники [Msg] образуют примерно такую иерархию:
 * ```
 * CompetitionMessage
 * |   PlayerTeam
 * |   |   TeamCreating
 * |   |   |   Error
 * |   |   |   |   TeamWithThatNameExists
 * |   |   |   |   IncorrectPin
 * |   |   |   TeamCreated
 * |   |   TeamJoining
 * |   |   |   Error
 * |   |   |   |   TeamIsFull
 * |   |   |   |   TeamDoesNotExist
 * |   |   |   YouJoinedTeam - отправляется игроку, который подключился к команде
 * |   |   |   NewPlayerInYourTeam - отправляется игрокам в этой команде
 * |   |   |   PlayerJoinedTeam - отправляется всем игрокам,
 * |   |   TeamMessageSending
 * |   |   |   YourMessageSent - отправляется игроку, который отправил сообщение
 * |   |   |   NewMessage - отправляется остальным игрокам в команде
 * ```
 */
@Suppress("FunctionName")
fun <P, Msg> GameMessage(player: P, message: Msg): GameMessage<P, Msg> =
    GameMessageImpl(player, message)

/**
 * Сообщение, отправив которое можно выполнить запрос [request] в игровой сессии.
 */
data class DeferredCommandRequest<out R : GameCommandRequest<*, *>>(
    val request: R,
    val timeoutMillis: Long = 0,
) : GameMessage<Nothing, Nothing>() {

    override val player: Nothing
        get() = error("This message is internal")

    override val body: Nothing
        get() = error("This message is internal")
}

@Serializable
internal data class GameMessageImpl<out P, out Msg>(
    @Polymorphic override val player: P,
    @Polymorphic override val body: Msg,
) : GameMessage<P, Msg>()

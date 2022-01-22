package ru.selemilka.game.core.base

/**
 * Сообщение, возвращаемое методом [GameRule.process] для взаимодействия с сессией
 *
 * Единственный способ создать свой объект GameMessage -
 * использовать фабричную функцию [GameMessage]
 */
sealed interface GameMessage<out P, out T> {
    val players: Set<P>
    val body: T
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
fun <P, Msg> GameMessage(players: Set<P>, message: Msg): GameMessage<P, Msg> =
    GameMessageImpl(players, message)

@Suppress("FunctionName")
fun <P, Msg> GameMessage(player: P, message: Msg): GameMessage<P, Msg> =
    GameMessageImpl(setOf(player), message)

/**
 * Сообщение, отправив которое можно выполнить запрос [request] в игровой сессии.
 */
data class DeferredCommandRequest<out CmdReq : GameCommandRequest<*, *>>(
    val request: CmdReq,
    val timeoutMillis: Long = 0,
) : GameMessage<Nothing, Nothing> {

    override val players: Nothing
        get() = error("This message is internal")

    override val body: Nothing
        get() = error("This message is internal")
}

internal data class GameMessageImpl<out P, out Msg>(
    override val players: Set<P>,
    override val body: Msg,
) : GameMessage<P, Msg>

internal interface InternalGameMessage<out P, out Msg> : GameMessage<P, Msg>

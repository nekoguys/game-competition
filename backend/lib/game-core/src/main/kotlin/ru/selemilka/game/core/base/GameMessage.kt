package ru.selemilka.game.core.base

/**
 * Сообщение, возвращаемое методом [GameRule.process] для взаимодействия с сессией
 *
 * Единственный способ создать свой объект GameMessage -
 * использовать фабричную функцию [GameMessage]
 */
sealed interface GameMessage<out P, out T> {
    val player: P
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
fun <P, Msg> GameMessage(player: P, message: Msg): GameMessage<P, Msg> =
    GameMessageImpl(player, message)

/**
 * Сообщение, отправив которое можно выполнить запрос [request] в игровой сессии.
 */
data class DeferredCommandRequest<out R : GameCommandRequest<*, *>>(
    val request: R,
    val timeoutMillis: Long = 0,
) : InternalGameMessage<Nothing, Nothing> {
    override val player: Nothing
        get() = error("This message is internal")

    override val body: Nothing
        get() = error("This message is internal")
}

/**
 * Тип всех внутренних игровых сообщений
 *
 * Игроки никогда не увидят эти сообщения, они нужны для функционирования игры.
 */
internal interface InternalGameMessage<out P, out Msg> : GameMessage<P, Msg>

internal data class GameMessageImpl<out P, out Msg>(
    override val player: P,
    override val body: Msg,
) : GameMessage<P, Msg>

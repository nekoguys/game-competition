package ru.selemilka.game.core.base

import kotlinx.coroutines.flow.Flow

/**
 * [Msg] - Событие, отправляемое игровым сервером игрокам.
 *
 * Наследники [Msg] тоже образуют иерархию, она похожа
 * на иерархию [Command], но более подробная.
 * Пример для игры конкуренция (см. пример для [Command])
 * ```
 * Message
 * |   CompetitionMessage
 * |   |   PlayerTeam
 * |   |   |   TeamCreating
 * |   |   |   |   Error
 * |   |   |   |   |   TeamWithThatNameExists
 * |   |   |   |   |   IncorrectPin
 * |   |   |   |   TeamCreated
 * |   |   |   TeamJoining
 * |   |   |   |   Error
 * |   |   |   |   |   TeamIsFull
 * |   |   |   |   |   TeamDoesNotExist
 * |   |   |   |   YouJoinedTeam - отправляется игроку, который подключился к команде
 * |   |   |   |   NewPlayerInYourTeam - отправляется игрокам в этой команде
 * |   |   |   |   PlayerJoinedTeam - отправляется всем игрокам,
 * |   |   |   TeamMessageSending
 * |   |   |   |   YourMessageSent - отправляется игроку, который отправил сообщение
 * |   |   |   |   NewMessage - отправляется остальным игрокам в команде
 * ```
 */

sealed interface TargetedMessage<out P, out Msg> {
    val player: P
    val message: Msg
}

@Suppress("FunctionName")
fun <P, Msg> TargetedMessage(player: P, message: Msg): TargetedMessage<P, Msg> =
    TargetedMessageImpl(player, message)

internal data class TargetedMessageImpl<out P, out Msg>(
    override val player: P,
    override val message: Msg
) : TargetedMessage<P, Msg>

internal object TerminalTargetedMessage : TargetedMessage<Nothing, Nothing> {
    override val player: Nothing
        get() = error("This targetedMessage was created artificially and doesn't have a receiver")
    override val message: Nothing
        get() = error("This targetedMessage was created artificially and doesn't have a message")
}

/**
 * Публичное API для отправки сообщений типа [Msg] игрокам.
 *
 * Классы-наследники задают, как именно отправляются сообщения:
 * * хранятся ли старые сообщения
 * * какие сообщения хранятся в памяти
 * * и т.д.
 */
interface MessageSource<P, out Msg> {
    fun getAllMessages(): Flow<TargetedMessage<P, Msg>>
    fun getMessages(player: P): Flow<Msg>
}

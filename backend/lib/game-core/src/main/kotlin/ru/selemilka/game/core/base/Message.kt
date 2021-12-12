package ru.selemilka.game.core.base

import kotlinx.coroutines.flow.Flow

/**
 * [Msg] - Событие, отправляемое игровым сервером игрокам.
 *
 * Наследники [Message] тоже образуют иерархию, она похожа
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
data class MessageToPlayer<out P, out Msg>(
    val player: P,
    val message: Msg,
)

/**
 * Публичное API для отправки сообщений типа [Msg] игрокам.
 *
 * Классы-наследники задают, как именно отправляются сообщения:
 * * хранятся ли старые сообщения
 * * какие сообщения хранятся в памяти
 * * и т.д.
 */
interface AnnouncementSource<P, out Msg> {
    fun getAnnouncements(): Flow<MessageToPlayer<P, Msg>>
    fun getAnnouncements(player: P): Flow<MessageToPlayer<P, Msg>>
}

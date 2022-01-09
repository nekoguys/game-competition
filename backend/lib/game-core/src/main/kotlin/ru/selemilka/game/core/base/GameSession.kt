package ru.selemilka.game.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Публичное API игры.
 *
 * Пользователи объекта [GameSession] могут:
 * * отправлять игре команды [Cmd] с помощью функции [accept]
 * * получать от игры сообщения [Msg] с помощью функций [getAllMessages], [getMessages]
 * * заканчивать игру с помощью функции [GameSession.close]
 *
 * Создать игру можно фабричным методом [CoroutineScope.launchGameSession]
 */
interface GameSession<P, in Cmd, out Msg> {
    /**
     * Обработка запроса [request] от пользователя.
     *
     * Приостанавливается до того момента, пока команда не обработается.
     * **Нельзя вызывать эту функцию в реализациях [GameRule]!**
     * Иначе не получится корректно закрыть игровую сессию.
     *
     * Если при обработке команды возникло исключение - оно бросается из этого метода
     */
    suspend fun accept(request: GameCommandRequest<P, Cmd>)

    /**
     * Возвращает все сообщения от этой игровой сессии
     */
    fun getAllMessages(): Flow<GameMessage<P, Msg>>
}

suspend fun <P, Cmd> GameSession<P, Cmd, *>.accept(
    player: P,
    command: Cmd,
) = accept(GameCommandRequest(player, command))

suspend fun GameSession<*, *, *>.close() = accept(CloseGameSessionRequest)

fun <P, Msg> GameSession<P, *, Msg>.getMessages(player: P): Flow<Msg> =
    getAllMessages()
        .filter { it !is InternalGameMessage<*, *> && it.player == player }
        .map { it.message }

@Suppress("FunctionName")
fun <P, Cmd, Msg> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, Msg>,
    onClose: () -> Unit = {},
): GameSession<P, Cmd, Msg> =
    GameSessionImpl(this, rule, onClose)


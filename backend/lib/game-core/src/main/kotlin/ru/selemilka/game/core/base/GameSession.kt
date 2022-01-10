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
interface GameSession<in CmdReq : GameCommandRequest<*, *>, out Msg : GameMessage<*, *>> {
    /**
     * Обработка запроса [request] от пользователя.
     *
     * Приостанавливается до того момента, пока команда не обработается.
     * **Нельзя вызывать эту функцию в реализациях [GameRule]!**
     * Иначе не получится корректно закрыть игровую сессию.
     *
     * Если при обработке команды возникло исключение - оно бросается из этого метода
     */
    suspend fun accept(request: CmdReq)

    /**
     * Возвращает все сообщения от этой игровой сессии
     */
    fun getAllMessages(): Flow<Msg>
}

suspend fun <P, Cmd> GameSession<GameCommandRequest<P, Cmd>, *>.accept(
    player: P,
    command: Cmd,
) = accept(GameCommandRequest(player, command))

suspend fun GameSession<GameCommandRequest<Nothing, Nothing>, *>.close() = accept(CloseGameSessionRequest)

fun <P, Msg> GameSession<*, GameMessage<P, Msg>>.getMessages(player: P): Flow<Msg> =
    getAllMessages()
        .filter { it.player == player }
        .map { it.body }

@Suppress("FunctionName")
fun <P, Cmd, Msg : GameMessage<P, *>> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, Msg>,
    onClose: () -> Unit = {},
): GameSession<GameCommandRequest<P, Cmd>, Msg> =
    GameSessionImpl(this, rule, onClose)


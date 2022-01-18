package ru.selemilka.game.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.selemilka.game.core.base.CloseGameSessionRequest
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameRule

/**
 * Публичное API игры.
 *
 * Пользователи объекта [GameSession] могут:
 * * отправлять игре команды [CmdReq] с помощью функции [accept]
 * * получать от игры сообщения [Msg] с помощью функций [getAllMessages], [getMessages]
 * * заканчивать игру с помощью функции [close]
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
    fun getAllMessagesIndexed(): Flow<IndexedValue<Msg>>
}

suspend fun <P, Cmd> GameSession<GameCommandRequest<P, Cmd>, *>.accept(
    player: P,
    command: Cmd,
) = accept(GameCommandRequest(player, command))

suspend fun GameSession<GameCommandRequest<Nothing, Nothing>, *>.close() = accept(CloseGameSessionRequest)

fun <Msg : GameMessage<*, *>> GameSession<*, Msg>.getAllMessages(): Flow<Msg> =
    getAllMessagesIndexed()
        .map { (_, value) -> value }

fun <P, T> GameSession<*, GameMessage<P, T>>.getMessages(player: P): Flow<T> =
    getAllMessages()
        .filter { player in it.players }
        .map { it.body }

@Suppress("FunctionName")
fun <P, Cmd, P2, T> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, GameMessage<P2, T>>,
    onClose: suspend () -> Unit = {},
    messageLog: GameMessageLog<P2, T>? = null,
    commandLog: GameCommandRequestLog<P, Cmd>? = null,
    traceIdProvider: TraceIdProvider? = null,
): GameSession<GameCommandRequest<P, Cmd>, GameMessage<P2, T>> {
    val sessionWithoutTracing = launchGameSession(rule, onClose, messageLog, commandLog)
    return if (traceIdProvider != null) {
        GameSessionWithTracing(sessionWithoutTracing, traceIdProvider)
    } else {
        sessionWithoutTracing
    }
}

@Suppress("FunctionName")
private fun <P, Cmd, P2, T> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, GameMessage<P2, T>>,
    onClose: suspend () -> Unit = {},
    messageLog: GameMessageLog<P2, T>? = null,
    commandLog: GameCommandRequestLog<P, Cmd>? = null,
): GameSession<GameCommandRequest<P, Cmd>, GameMessage<P2, T>> {
    val sessionWithoutCommandLogging = launchGameSession(rule, onClose, messageLog)
    return if (commandLog != null) {
        GameSessionWithCommandLogging(sessionWithoutCommandLogging, commandLog)
    } else {
        sessionWithoutCommandLogging
    }
}

@Suppress("FunctionName")
private fun <P, Cmd, P2, T> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, GameMessage<P2, T>>,
    onClose: suspend () -> Unit = {},
    messageLog: GameMessageLog<P2, T>? = null,
): GameSession<GameCommandRequest<P, Cmd>, GameMessage<P2, T>> {
    return if (messageLog != null) {
        GameSessionWithMessageLogging(
            interceptedSession = launchGameSession(
                rule = GameRuleWithMessageLogging(rule, messageLog),
                onClose = onClose,
                replay = 1,
            ),
            messageLog = messageLog,
        )
    } else {
        launchGameSession(rule, onClose, replay = Int.MAX_VALUE)
    }
}

@Suppress("FunctionName")
private fun <P, Cmd, P2, T> CoroutineScope.launchGameSession(
    rule: GameRule<P, Cmd, GameMessage<P2, T>>,
    onClose: suspend () -> Unit = {},
    replay: Int,
): GameSession<GameCommandRequest<P, Cmd>, GameMessage<P2, T>> {
    return SimpleGameSession(
        parentScope = this,
        rule = rule,
        onClose = onClose,
        replay = replay,
    )
}

package ru.nekoguys.game.core.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import ru.nekoguys.game.core.CloseGameSessionRequest
import ru.nekoguys.game.core.GameCommandRequest
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Публичное API игры.
 *
 * Пользователи объекта [GameSession] могут:
 * * отправлять игре команды [Cmd] с помощью функции [accept]
 * * получать от игры сообщения [Msg] с помощью функций [getAllMessages], [getMessages]
 * * заканчивать игру с помощью функции [close]
 *
 * Создать игру можно фабричным методом [createGameSession]
 */
interface GameSession<in P, in Cmd, out P2, out Msg> {
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
    fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>>
}

suspend fun <P, Cmd> GameSession<P, Cmd, *, *>.accept(
    player: P,
    command: Cmd,
) = accept(GameCommandRequest(player, command))

suspend fun GameSession<*, *, *, *>.close() =
    accept(CloseGameSessionRequest)

fun <P2, Msg> GameSession<*, *, P2, Msg>.getAllMessages(): Flow<GameMessage<P2, Msg>> =
    getAllMessagesIndexed()
        .map { (_, value) -> value }

fun <P2, Msg> GameSession<*, *, P2, Msg>.getMessages(player: P2): Flow<Msg> =
    getAllMessages()
        .filter { player in it.players }
        .map { it.body }


@Suppress("FunctionName")
fun <P, Cmd, P2, Msg> createGameSession(
    rule: GameRule<P, Cmd, P2, Msg>,
    parentContext: CoroutineContext = EmptyCoroutineContext,
    replay: Int? = null,
    messageLog: GameMessageLog<P2, Msg>? = null,
    commandLog: GameCommandRequestLog<P, Cmd>? = null,
    onClose: suspend () -> Unit = {},
): GameSession<P, Cmd, P2, Msg> {
    val wrappers: List<InternalGameSessionWrapper<P, Cmd, P2, Msg>> = buildList {
        if (messageLog != null) {
            add { GameSessionWithMessageLogging(it, messageLog) }
        }
        if (commandLog != null) {
            add { GameSessionWithCommandLogging(it, commandLog) }
        }
        add(::GameSessionWithDeferredCommands)
        add { CloseableGameSession(it, parentContext, onClose) }
        if (messageLog != null) {
            add { GameSessionWithMessagesFromLog(it, messageLog) }
        }
    }

    val baseSession: InternalGameSession<P, Cmd, P2, Msg> = BaseGameSession(
        rule = rule,
        replay = replay ?: if (messageLog == null) Int.MAX_VALUE else 1,
    )

    return wrappers.fold(baseSession) { s, wrapper -> wrapper(s) }
}

internal interface InternalGameSession<in P, in Cmd, P2, Msg>
    : GameSession<P, Cmd, P2, Msg> {

    override suspend fun accept(request: GameCommandRequest<P, Cmd>) {
        acceptAndReturnMessages(request)
    }

    /**
     * Обрабатывает команду и возвращает результат обработки - сообщения игрокам
     */
    suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>>

    suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>)
}

internal typealias InternalGameSessionWrapper<P, Cmd, P2, Msg> =
            (InternalGameSession<P, Cmd, P2, Msg>) -> InternalGameSession<P, Cmd, P2, Msg>

package ru.selemilka.game.core.base

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap


typealias SessionId = Long

/**
 * Who requested an action
 */
interface ActionInitiator

/**
 * Describes an interaction of the user with the game.
 */
sealed interface AnyAction {
    val initiator: ActionInitiator
}

interface Action<out I : ActionInitiator> : AnyAction {
    override val initiator: I
}

/**
 * Публичное API для получения сообщений [A] от игроков.
 *
 * Классы-наследники задают, как именно получаются сообщения:
 * * обрабатываются ли сообщения параллельно
 * * какие сообщения обрабатываются последовательно
 * * и т.д.
 */
interface ActionConsumer<in A : AnyAction> {
    suspend fun consume(id: SessionId, action: A)
}


/**
 * Функция, вызываемая для обработки действия в реализациях [ActionConsumer]
 */
typealias OnActionCall<A> = suspend (SessionId, A) -> Unit

/**
 * Простая реализация [ActionConsumer].
 *
 * Просто вызывает [onAction] для каждого сообщения.
 */
class SimpleActionConsumer<in A : AnyAction>(
    private val onAction: OnActionCall<A>,
) : ActionConsumer<A> {
    /**
     * Эта функция приостанавливается до того момента,
     * пока входящее сообщение не выполнится.
     *
     * Если при обработке сообщения возникло исключение - оно бросается из этого метода
     */
    override suspend fun consume(id: SessionId, action: A) {
        onAction(id, action)
    }
}

/**
 * "Честная" реализация [ActionConsumer]
 *
 * Свойства:
 * * запросы из разных сессий обрабатываются параллельно
 * * запросы в одной сессии обрабатываются в порядке поступления
 * * действие [onAction] вызывается в отдельной корутине
 *
 * Называется честной, потому что первым обработается действие, произошедшее раньше.
 *
 * FIXME: может быть полезно параллельно обрабатывать действия в рамках одной сессии.
 *  К примеру, действия разных команд, действия учителя и учеников.
 *  Можно расширить этот класс и дать возможность указывать,
 *  что ещё можно обрабатывать параллельно.
 */
class FairActionConsumer<in A : AnyAction>(
    private val onAction: OnActionCall<A>,
) : ActionConsumer<A> {

    private data class ActionRequest<A : AnyAction>(
        val action: A,
        val ack: CompletableDeferred<Unit>,
    )

    /**
     * FIXME: тут есть проблема - каналы только добавляются, но не удаляются.
     *  Можно попробовать использовать какой-то умный кэш,
     *  где бездействующие каналы будут убиваться.
     *  Ну или же можно сделать это всё проще, я ещё не понял
     */
    private val requestChannels = ConcurrentHashMap<SessionId, Channel<ActionRequest<A>>>()

    override suspend fun consume(id: SessionId, action: A) = coroutineScope {
        val ack = CompletableDeferred<Unit>()
        requestChannels
            .computeIfAbsent(id) { createChannel(id) }
            .send(ActionRequest(action, ack))
        ack.await()
    }

    private fun CoroutineScope.createChannel(id: SessionId): Channel<ActionRequest<A>> {
        val requests = Channel<ActionRequest<A>>()
        launch {
            for ((action, ack) in requests) {
                val result = runCatching { onAction(id, action) }
                ack.completeWith(result)
            }
        }
        return requests
    }
}

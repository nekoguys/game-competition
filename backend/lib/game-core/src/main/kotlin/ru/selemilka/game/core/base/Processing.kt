package ru.selemilka.game.core.base

import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast


/**
 * Публичное API для разработчиков игры.
 *
 * Классы-наследники задают, какие реакции отправляются в ответ на запрос.
 *
 * Реализация [Processor] нужна, чтобы создать свою игру с помощью функции [createGame].
 */
interface Processor<in A : AnyAction, out R : AnyReaction> {
    /**
     *
     */
    suspend fun process(id: SessionId, action: A): List<R>

    /**
     * Обрабатывает запрос или возвращает null, если не умеет его обрабатывать
     */
    suspend fun tryProcess(id: SessionId, action: AnyAction): List<R>?
}

interface TypedProcessor<A : AnyAction, out R : AnyReaction> : Processor<A, R> {
    val actionClass: KClass<A>

    override suspend fun tryProcess(id: SessionId, action: AnyAction): List<R>? =
        actionClass
            .safeCast(action)
            ?.let { castedAction -> process(id, castedAction) }
}

/**
 * Реализация [Processor], делегирующая работу процессорам из [processors]
 *
 * Стратегии из [processors] поочерёдно применяются к входному запросу типа [A].
 */
@PublishedApi
internal class CompositeProcessor<A : AnyAction, out R : AnyReaction>(
    override val actionClass: KClass<A>,
    private val processors: List<Processor<A, R>>,
) : TypedProcessor<A, R> {
    override suspend fun process(id: SessionId, action: A): List<R> =
        processors
            .mapNotNull { strategy -> strategy.tryProcess(id, action) }
            .flatten() // TODO: подумать про обработку ошибок.
}

inline fun <reified A : AnyAction, R : AnyReaction> composeProcessors(
    vararg processors: Processor<A, R>,
): Processor<A, R> = CompositeProcessor(
    actionClass = A::class,
    processors = processors.toList(),
)
//
//inline fun <reified A : AnyAction, R : AnyReaction> strategy(
//    noinline processing: suspend (SessionId, A) -> List<R>,
//): Processor<A, R> {
//    return object : Processor<A, R> {
//        override suspend fun process(id: SessionId, action: A): List<R> {
//            return processing(id, action)
//        }
//
//        override suspend fun tryProcess(id: SessionId, action: AnyAction): List<R>? {
//            return A::class
//                .safeCast(action)
//                ?.let { castedAction -> tryProcess(id, castedAction) }
//        }
//    }
//}
//
//inline fun <A : AnyAction, R : AnyReaction, reified NewA : AnyAction> Processor<A, R>.mappingAction(
//    crossinline actionMapping: (NewA) -> A,
//): Processor<NewA, R> =
//    strategy { id, action ->
//        process(id, actionMapping(action))
//    }
//
//inline fun <reified A : AnyAction, R : AnyReaction, NewR : AnyReaction> Processor<A, R>.mapReaction(
//    crossinline mapReaction: (R) -> NewR,
//): Processor<A, NewR> =
//    strategy { id, action ->
//        process(id, action).map { mapReaction(it) }
//    }

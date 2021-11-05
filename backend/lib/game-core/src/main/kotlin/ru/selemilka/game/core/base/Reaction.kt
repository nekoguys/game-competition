package ru.selemilka.game.core.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlin.reflect.KClass

/**
 * A destination for the announcement.
 */
interface ReactionScope {
    /**
     * [ReactionScope] для ответов, предназначенных всем игрокам.
     * Часто используется, поэтому находится прямо в [ru.selemilka.game.core.base]
     */
    data class All(val sessionId: SessionId) : ReactionScope

    /**
     * [ReactionScope] для ответов, которые надо отправить отправителю.
     * Часто используется, поэтому находится прямо в [ru.selemilka.game.core.base]
     */
    interface Initiator : ReactionScope {
        val initiator: ActionInitiator
    }
}

/**
 * Какая-то реакция, отправляемая пользователю
 */
sealed interface AnyReaction {
    val scope: ReactionScope
}

interface Reaction<out S : ReactionScope> : AnyReaction {
    override val scope: S
}


/**
 * Публичное API для отправки сообщений типа [R] игрокам.
 *
 * Классы-наследники задают, как именно отправляются сообщения:
 * * хранятся ли старые сообщения
 * * какие сообщения хранятся в памяти
 * * и т.д.
 */
interface ReactionProducer<out R : AnyReaction> {
    fun allReactions(): Flow<R>
    fun <T : ReactionScope> reactions(scopeClass: KClass<T>): Flow<Reaction<T>>
}


interface MutableReactionProducer<R : AnyReaction> : ReactionProducer<R> {
    suspend fun shareReactions(reactions: Collection<R>)
}

class SimpleReactionProducer<R : AnyReaction> : MutableReactionProducer<R> {
    private val mutableReactions = MutableSharedFlow<R>()

    override suspend fun shareReactions(reactions: Collection<R>) {
        reactions
            .onEach { mutableReactions.emit(it) }
    }

    override fun allReactions(): Flow<R> =
        mutableReactions
            .asSharedFlow()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ReactionScope> reactions(scopeClass: KClass<T>): Flow<Reaction<T>> =
        allReactions()
            .filter { reaction -> scopeClass.isInstance(reaction.scope) }
            .let { it as Flow<Reaction<T>> } // кастуем к ReactionWithScope, других реакций не бывает
}


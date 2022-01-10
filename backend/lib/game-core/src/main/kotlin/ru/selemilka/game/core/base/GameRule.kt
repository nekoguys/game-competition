package ru.selemilka.game.core.base

import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Описание логики обработки команды.
 *
 * Команды обрабатываются параллельно в соответствии с результатом функции [getLocksFor]
 */
interface GameRule<in P, in Cmd, out Msg : GameMessage<*, *>> {
    suspend fun process(player: P, command: Cmd): List<Msg>

    /**
     * Блокировки, которые нужно взять для обработки [command]
     *
     * Это необходимо для того, чтобы не допустить race condition'ы.
     */
    suspend fun getLocksFor(command: Cmd): ResourceLocks =
        ResourceLocks(
            shared = sortedSetOf(),
            unique = defaultLockableResource,
        )
}

open class LockableResource private constructor(
    private val num: Long,
) : Comparable<LockableResource> {
    constructor() : this(numCounter.getAndIncrement())

    override fun compareTo(other: LockableResource): Int =
        num compareTo other.num

    companion object {
        private val numCounter = AtomicLong()
    }
}

private val defaultLockableResource = sortedSetOf(LockableResource())

// TODO: публичные мутабельные поля - плохо
//  но на первое время пойдёт
data class ResourceLocks(
    val shared: SortedSet<LockableResource>,
    val unique: SortedSet<LockableResource>,
) {
    init {
        require((shared intersect unique).isEmpty())
    }

    operator fun plus(other: ResourceLocks) =
        ResourceLocks(
            shared = shared
                .toSortedSet()
                .apply { addAll(other.shared) },
            unique = unique
                .toSortedSet()
                .apply { addAll(other.unique) }
        )
}

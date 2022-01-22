package ru.selemilka.game.core.session

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import ru.selemilka.game.core.base.ResourceLocks
import java.util.concurrent.ConcurrentHashMap

internal class ResourceLockProvider {
    private val locks = ConcurrentHashMap<Any, ReadWriteLock>()

    suspend fun <T> useLocks(
        lockedResources: ResourceLocks,
        action: suspend () -> T,
    ): T {
        val readLocks = lockedResources.shared.map { key -> locks.getOrPut(key, ::ReadWriteLock) }
        val writeLocks = lockedResources.unique.map { key -> locks.getOrPut(key, ::ReadWriteLock) }

        withContext(NonCancellable) {
            readLocks.forEach { it.lockRead() }
            writeLocks.forEach { it.lockWrite() }
        }

        return try {
            action()
        } finally {
            withContext(NonCancellable) {
                readLocks.forEach { lock -> lock.unlockRead() }
                writeLocks.forEach { lock -> lock.unlockWrite() }
            }
        }
    }
}

/**
 * В стандартной библиотеке такого нет
 * Взял из [википедии](https://en.wikipedia.org/wiki/Readers–writer_lock#Using_two_mutexes)
 */
private class ReadWriteLock {
    private var blockingReaders = 0
    private val readMutex = Mutex(locked = false)
    private val writeMutex = Mutex(locked = false)

    suspend fun lockRead() {
        readMutex.lock()
        blockingReaders += 1
        if (blockingReaders == 1) {
            writeMutex.lock()
        }
        readMutex.unlock()
    }

    suspend fun unlockRead() {
        readMutex.lock()
        blockingReaders -= 1
        if (blockingReaders == 0) {
            writeMutex.unlock()
        }
        readMutex.unlock()
    }

    suspend fun lockWrite() {
        writeMutex.lock()
    }

    fun unlockWrite() {
        writeMutex.unlock()
    }
}

package ru.selemilka.game.rps.storage

import org.springframework.stereotype.Repository
import ru.selemilka.game.core.base.LockableResource
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.model.RpsStage
import java.util.concurrent.atomic.AtomicLong

interface RpsSessionStorage {
    suspend fun createSession(settings: RpsSessionSettings): RpsSession.Id
    suspend fun loadSession(id: RpsSession.Id): RpsSession?
    suspend fun sessionExists(id: RpsSession.Id): Boolean

    suspend fun saveStage(id: RpsSession.Id, stage: RpsStage)
    suspend fun loadStage(id: RpsSession.Id): RpsStage?

    suspend fun loadSettings(id: RpsSession.Id): RpsSessionSettings?

    companion object ResourceKey : LockableResource()
}

@Repository
class RpsSessionInMemoryStorage : RpsSessionStorage {
    private val sessions = mutableMapOf<RpsSession.Id, RpsSession>()
    private var sessionIdInc = AtomicLong(0)

    override suspend fun createSession(settings: RpsSessionSettings): RpsSession.Id {
        val newId = RpsSession.Id(sessionIdInc.incrementAndGet())
        val newSession = RpsSession(
            id = newId,
            stage = RpsStage.PLAYERS_JOINING,
            settings = settings,
        )
        sessions[newId] = newSession
        return newSession.id
    }

    override suspend fun loadSession(id: RpsSession.Id): RpsSession? =
        sessions[id]

    override suspend fun sessionExists(id: RpsSession.Id): Boolean =
        id in sessions

    override suspend fun saveStage(id: RpsSession.Id, stage: RpsStage) {
        val session = sessions[id]
        checkNotNull(session) { "Non-existent session cannot be updated" }

        sessions[id] = session.copy(stage = stage)
    }

    override suspend fun loadStage(id: RpsSession.Id): RpsStage? =
        sessions[id]?.stage

    override suspend fun loadSettings(id: RpsSession.Id): RpsSessionSettings? =
        sessions[id]?.settings
}

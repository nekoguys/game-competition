package ru.selemilka.game.rps.util

import org.springframework.stereotype.Component
import ru.selemilka.game.core.session.TraceId
import ru.selemilka.game.core.session.TraceIdProvider
import ru.selemilka.game.rps.model.RpsSession
import java.util.concurrent.ConcurrentHashMap

@Component
class TraceIdProviderFactory {
    private val traceIdBySessionId = ConcurrentHashMap<RpsSession.Id, Long>()

    fun getProvider(sessionId: RpsSession.Id) = TraceIdProvider {
        val traceIdNum = traceIdBySessionId.compute(sessionId) { _, v -> (v ?: -1) + 1 }
        TraceId("$sessionId-$traceIdNum")
    }
}

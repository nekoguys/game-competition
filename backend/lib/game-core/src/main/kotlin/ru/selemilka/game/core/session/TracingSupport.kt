package ru.selemilka.game.core.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage

@JvmInline
value class TraceId(val value: String)

fun interface TraceIdProvider {
    suspend fun generateTraceId(): TraceId
}

fun currentTraceIdOrNull(): TraceId? = MDC.get("traceId")?.let(::TraceId)

internal class GameSessionWithTracing<CmdReq : GameCommandRequest<*, *>, Msg : GameMessage<*, *>>(
    private val interceptedSession: GameSession<CmdReq, Msg>,
    private val traceIdProvider: TraceIdProvider,
) : GameSession<CmdReq, Msg> {

    override suspend fun accept(request: CmdReq) {
        val traceId = traceIdProvider.generateTraceId()
        MDC.putCloseable("traceId", traceId.value).use {
            withContext(MDCContext()) {
                interceptedSession.accept(request)
            }
        }
    }

    override fun getAllMessagesIndexed(): Flow<IndexedValue<Msg>> =
        interceptedSession.getAllMessagesIndexed()
}

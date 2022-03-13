package ru.nekoguys.game.web.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.http.codec.ServerSentEvent
import reactor.util.context.ContextView
import kotlin.coroutines.coroutineContext

const val REQUEST_ID_CONTEXT_KEY: String = "requestId"

fun ContextView.extractRequestId(): String =
    get(REQUEST_ID_CONTEXT_KEY)

suspend fun <T> withMDCContext(block: suspend () -> T): T {
    val requestId: String? =
        coroutineContext[ReactorContext]
            ?.context
            ?.extractRequestId()

    return if (requestId != null) {
        // внешний withContext очистит MDC после своего выполнения
        withContext(MDCContext()) {
            // во внутреннем withContext в MDC будет лежать requestId
            val contextMap = mapOf(REQUEST_ID_CONTEXT_KEY to requestId)
            withContext(MDCContext(contextMap)) {
                block()
            }
        }
    } else {
        block()
    }
}

fun <T> Flow<T>.wrapToServerSentEvent(streamName: String): Flow<ServerSentEvent<T>> =
    this.map { ServerSentEvent.builder(it).id(streamName).build() } //TODO почему ругается?
//Warning:(38, 40) Type mismatch: value of a nullable type T is used where non-nullable type is expected. This warning will become an error soon. See https://youtrack.jetbrains.com/issue/KT-36770 for details

fun <T> Flow<T>.withRequestIdInContext(): Flow<T> =
    flow {
        val requestId: String? =
            currentCoroutineContext()[ReactorContext]
                ?.context
                ?.extractRequestId()

        if (requestId != null) {
            val contextMap = mapOf(REQUEST_ID_CONTEXT_KEY to requestId)
            val flowWithMDCContext = this@withRequestIdInContext
                .flowOn(MDCContext(contextMap))
            emitAll(flowWithMDCContext)
        } else {
            emitAll(this@withRequestIdInContext)
        }
    }.flowOn(MDCContext(emptyMap()))

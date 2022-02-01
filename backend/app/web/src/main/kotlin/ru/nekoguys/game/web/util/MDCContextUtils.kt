package ru.nekoguys.game.web.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import reactor.util.context.ContextView
import kotlin.coroutines.coroutineContext

const val REQUEST_ID_CONTEXT_KEY: String = "requestId"

fun ContextView.extractRequestId(): String =
    get(REQUEST_ID_CONTEXT_KEY)

suspend fun <T> withMDCContext(block: suspend CoroutineScope.() -> T): T {
    val traceId: String? =
        coroutineContext[ReactorContext]
            ?.context
            ?.extractRequestId()

    MDC.put(REQUEST_ID_CONTEXT_KEY, traceId)

    return withContext(MDCContext()) { block() }
}

package ru.nekoguys.game.web.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import reactor.util.context.ContextView
import kotlin.coroutines.coroutineContext

const val REQUEST_ID_CONTEXT_KEY: String = "requestId"

fun ContextView.extractRequestId(): String =
    get(REQUEST_ID_CONTEXT_KEY)

fun ContextView.extractRequestIdPrefix(): String =
    " [${extractRequestId()}]"

suspend fun <T> withMDCContext(block: suspend () -> T): T {
    val logPrefix: String? =
        coroutineContext[ReactorContext]
            ?.context
            ?.extractRequestIdPrefix()

    return if (logPrefix != null) {
        // внешний withContext очистит MDC после своего выполнения
        withContext(MDCContext()) {
            // во внутреннем withContext в MDC будет лежать requestId
            val contextMap = mapOf(REQUEST_ID_CONTEXT_KEY to logPrefix)
            withContext(MDCContext(contextMap)) {
                block()
            }
        }
    } else {
        block()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.withRequestIdInContext(): Flow<T> =
    channelFlow {
        withMDCContext {
            this@withRequestIdInContext
                .collect { send(it) }
        }
    }

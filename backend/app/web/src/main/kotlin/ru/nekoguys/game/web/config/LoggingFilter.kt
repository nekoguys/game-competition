package ru.nekoguys.game.web.config

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.util.REQUEST_ID_CONTEXT_KEY
import ru.nekoguys.game.web.util.extractRequestId

/**
 * Запись в лог каждого запроса и ответа
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class LoggingFilter : WebFilter {
    private val logger = LoggerFactory.getLogger(LoggingFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()

        val logRequestMono: Mono<Void> = Mono.deferContextual { context ->
            val requestId = context.extractRequestId()
            MDC.putCloseable(REQUEST_ID_CONTEXT_KEY, requestId).use {
                logRequest(exchange.request)
            }
            Mono.empty()
        }

        return logRequestMono
            .then(chain.filter(exchange))
            .doOnEach { signal ->
                val requestId = signal.contextView.extractRequestId()
                MDC.putCloseable(REQUEST_ID_CONTEXT_KEY, requestId).use {
                    when {
                        signal.isOnComplete -> {
                            logResponse(exchange.response)
                            logExecutionTime(startTime)
                        }
                        signal.isOnError -> {
                            logError(signal.throwable!!, exchange.response)
                            logExecutionTime(startTime)
                        }
                    }
                }
            }
    }

    private fun logRequest(request: ServerHttpRequest) {
        val method = request.method
        val uri = request.uri.rawPath
        logger.info("Started processing {} {}", method, uri)
    }

    private fun logResponse(response: ServerHttpResponse) {
        val statusCode = response.statusCode
        logger.info("Finished processing with response {}", statusCode)
    }

    /**
     * Логируются все ошибки, кроме 500
     * Они уже логируются в [org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler.logError]
     */
    private fun logError(ex: Throwable, response: ServerHttpResponse) {
        if (response.statusCode != HttpStatus.INTERNAL_SERVER_ERROR) {
            logger.error("Finished processing exceptionally", ex)
        }
    }

    private fun logExecutionTime(startTimeMillis: Long) {
        val elapsedMillis = System.currentTimeMillis() - startTimeMillis
        logger.info("$elapsedMillis ms elapsed")
    }
}

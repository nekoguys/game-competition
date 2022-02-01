package ru.nekoguys.game.web.config

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.util.REQUEST_ID_CONTEXT_KEY
import ru.nekoguys.game.web.util.extractRequestId

@Component
@Order(1)
class LogRequestAndResponseFilter : WebFilter {

    private val logger = LoggerFactory.getLogger(LogRequestAndResponseFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()

        val logRequestMono: Mono<Void> = Mono.deferContextual { context ->
            val requestId = context.extractRequestId()
            MDC.putCloseable(REQUEST_ID_CONTEXT_KEY, requestId).use {
                logRequest(exchange.request)
            }
            Mono.empty()
        }

        val logResponseMono: Mono<Void> = Mono.deferContextual { context ->
            val requestId = context.extractRequestId()
            MDC.putCloseable(REQUEST_ID_CONTEXT_KEY, requestId).use {
                logOnResponse(startTime, exchange)
            }
            Mono.empty()
        }

        return logRequestMono
            .then(chain.filter(exchange))
            .then(logResponseMono)
    }

    private fun logRequest(request: ServerHttpRequest) {
        val method = request.method
        val uri = request.uri
        logger.info("Got {} request {}", method, uri)
    }

    private fun logOnResponse(startTime: Long, exchange: ServerWebExchange) {
        val statusCode = exchange.response.statusCode
        val time = System.currentTimeMillis()
        val timeElapsedSeconds = ((time - startTime) / 1000.0)

        logger.info("Got response {} in {} seconds", statusCode, timeElapsedSeconds)
    }
}

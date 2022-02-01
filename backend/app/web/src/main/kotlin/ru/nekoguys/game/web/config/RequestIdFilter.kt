package ru.nekoguys.game.web.config

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.util.REQUEST_ID_CONTEXT_KEY
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicLong

/**
 * Для каждого запроса генерируется RequestId и кладётся в контекст
 *
 * Номера запросов монотонно возрастают (если не перезапускать приложение)
 * При перезапуске приложения такая ситуация тоже почти наверняка не возникнет
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestIdFilter : WebFilter {

    private val lastRequestId = AtomicLong(calculateInitialRequestId())

    private fun calculateInitialRequestId(): Long {
        val startPoint = LocalDateTime.of(2022, 1, 1, 0, 0)
        val today = LocalDateTime.now()
        val secondsElapsed = today.toEpochSecond(ZoneOffset.UTC) - startPoint.toEpochSecond(ZoneOffset.UTC)

        // Считаем, что в среднем приходит не больше 10 запросов в секунду
        // Тогда requestId всегда будет монотонно возрастать
        return secondsElapsed * 10
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestId = exchange.request.headers
            .extractRequestId() ?: generateRequestId()
        val requestIdString = requestId.toString()

        exchange.attributes[ServerWebExchange.LOG_ID_ATTRIBUTE] = requestIdString
        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put(REQUEST_ID_CONTEXT_KEY, requestIdString) }
    }

    private fun HttpHeaders.extractRequestId(): Long? =
        get("X-Request-ID")
            ?.first()
            ?.trim()
            ?.toLongOrNull()

    private fun generateRequestId(): Long =
        lastRequestId.getAndIncrement()
}

package ru.nekoguys.game.web.config

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.util.REQUEST_ID_CONTEXT_KEY

/**
 * ID каждого запроса кладётся в контекст
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestIdFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val requestId = exchange.attributes[ServerWebExchange.LOG_ID_ATTRIBUTE]
        val requestIdString = " [${requestId}]"
        return chain.filter(exchange)
            .contextWrite { ctx -> ctx.put(REQUEST_ID_CONTEXT_KEY, requestIdString) }
    }
}

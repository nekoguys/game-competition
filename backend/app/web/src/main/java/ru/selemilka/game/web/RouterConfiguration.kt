package ru.selemilka.game.web

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class RouterConfiguration {
    @Bean
    fun gameRoutes() = coRouter {
        GET("/hello/world").invoke {
            ServerResponse.ok()
                .body(BodyInserters.fromValue("1231"))
                .awaitSingle()
        }
    }
}
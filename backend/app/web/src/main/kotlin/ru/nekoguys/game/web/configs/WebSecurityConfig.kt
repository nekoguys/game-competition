package ru.nekoguys.game.web.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class WebSecurityConfig {
    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity) = http {
        cors { disable() }
        csrf { disable() }
        authorizeExchange {
            authorize(anyExchange, permitAll)
        }
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}

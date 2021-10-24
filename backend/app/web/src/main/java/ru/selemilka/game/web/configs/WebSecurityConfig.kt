package ru.selemilka.game.web.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class WebSecurityConfig {
    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.cors().disable()
                .csrf().disable()
                .authorizeExchange().anyExchange().permitAll()
                .and().build()
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder? {
        return BCryptPasswordEncoder()
    }
}

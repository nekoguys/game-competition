package ru.nekoguys.game.web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono
import ru.nekoguys.game.web.security.jwt.JwtAuthenticationConverter

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
class WebSecurityConfig(
    private val jwtAuthenticationConverter: JwtAuthenticationConverter,
) {
    @Bean
    fun springWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain = http {
        cors {
            configurationSource = UrlBasedCorsConfigurationSource().apply {
                registerCorsConfiguration(
                    "/**",
                    CorsConfiguration().applyPermitDefaultValues()
                )
            }
        }

        csrf { disable() }

        exceptionHandling {
            authenticationEntryPoint = HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)
            accessDeniedHandler = HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)
        }

        authorizeExchange {
            authorize("/api/auth/**", permitAll)
            authorize(anyExchange, authenticated)
        }

        addFilterBefore(
            jwtAuthenticationFilter(),
            SecurityWebFiltersOrder.AUTHORIZATION,
        )
    }

    @Bean
    fun reactiveAuthenticationManager(
        userDetailsService: GameUserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): ReactiveAuthenticationManager {
        val manager = UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
        manager.setPasswordEncoder(passwordEncoder)
        return manager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()

    private fun jwtAuthenticationFilter(): AuthenticationWebFilter {
        // Так как вся логика по проверке аутентификации
        // находится в jwtAuthenticationConverter,
        // эта реализация ReactiveAuthenticationManager просто принимает любую
        // попытку аутентификации
        val dummyAuthManager = ReactiveAuthenticationManager { Mono.just(it) }

        return AuthenticationWebFilter(dummyAuthManager)
            .apply { setServerAuthenticationConverter(jwtAuthenticationConverter) }
    }
}

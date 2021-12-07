package ru.selemilka.game

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import ru.selemilka.game.user.postgres.converters.UserReadingConverter

@Configuration
@EnableR2dbcRepositories
class R2dbcRepositoriesConfig {
    @Bean
    fun customConversion() : R2dbcCustomConversions {
        return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                listOf(
                    UserReadingConverter()
                )
        )
    }
}

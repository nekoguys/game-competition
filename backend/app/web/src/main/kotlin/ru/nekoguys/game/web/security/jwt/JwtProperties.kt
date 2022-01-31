package ru.nekoguys.game.web.security.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "game.app.web.jwt")
class JwtProperties {
    var secret: String = "default-secret"
    var expirationSeconds: Long = 0
}

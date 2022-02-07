package ru.nekoguys.game.web.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val properties: JwtProperties,
) {
    private val logger = LoggerFactory.getLogger(JwtProvider::class.java)

    private val jwtSecretKey: SecretKey =
        calculateSecretKey(properties.secret)

    private fun calculateSecretKey(secret: String?): SecretKey =
        if (secret.isNullOrBlank()) {
            Keys.secretKeyFor(SignatureAlgorithm.HS256)
        } else {
            Keys.hmacShaKeyFor(secret.toByteArray())
        }

    private val jwtParser: JwtParser =
        Jwts.parserBuilder().setSigningKey(jwtSecretKey).build()

    fun generateExpirationTimestamp(): Long =
        Date().time + properties.expirationSeconds * 1000

    fun generateJwtToken(authentication: Authentication, expirationTimestamp: Long): String {
        val roles = authentication
            .authorities
            .joinToString(";") { "$it".removePrefix("ROLE_") }

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim("roles", roles)
            .setIssuedAt(Date()) // не совсем корректный способ
            .setExpiration(Date(expirationTimestamp))
            .signWith(jwtSecretKey)
            .compact()
    }

    fun parseJwt(token: String): Jws<Claims>? {
        try {
            return jwtParser.parseClaimsJws(token)
        } catch (e: SecurityException) {
            logger.debug("Invalid JwtSignature", e)
        } catch (e: MalformedJwtException) {
            logger.debug("Invalid JwtToken", e)
        } catch (e: ExpiredJwtException) {
            logger.debug("Expired JWT token", e)
        } catch (e: UnsupportedJwtException) {
            logger.debug("Unsupported JWT token", e)
        } catch (e: IllegalArgumentException) {
            logger.debug("JWT claims string $token is empty", e)
        }
        return null
    }
}

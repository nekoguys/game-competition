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
    private val jwtSecretKey: SecretKey =
        Keys.hmacShaKeyFor(properties.secret.toByteArray())

    private val jwtParser: JwtParser =
        Jwts.parserBuilder().setSigningKey(jwtSecretKey).build()

    fun currentExpirationTimestamp(): Long =
        Date().time + properties.expirationSeconds * 1000

    fun generateJwtToken(authentication: Authentication, expirationTimestamp: Long): String {
        val roles = authentication
            .authorities
            .joinToString(";") { it.toString().removePrefix("ROLE_") }
        return Jwts.builder()
            .setSubject(authentication.name)
            .claim("roles", roles)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + properties.expirationSeconds * 1000))
            .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
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

    private companion object {
        private val logger = LoggerFactory.getLogger(JwtProvider::class.java)
    }
}

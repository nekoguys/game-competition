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
        Keys.hmacShaKeyFor(properties.secret.toByteArray());

    private val jwtParser: JwtParser =
        Jwts.parserBuilder().setSigningKey(jwtSecretKey).build()

    fun generateJwtToken(authentication: Authentication): String {
        return Jwts.builder()
            .setSubject(authentication.name)
//            .claim("roles", authentication.authorities.joinToString(", "))
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + properties.expirationSeconds * 1000))
            .signWith(jwtSecretKey, SignatureAlgorithm.HS512)
            .compact()
    }

    fun parseJwt(token: String): Jws<Claims>? {
        try {
            return jwtParser.parseClaimsJws(token)
        } catch (e: SecurityException) {
            logger.error("Invalid JwtSignature", e.stackTraceToString())
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JwtToken", e)
        } catch (e: ExpiredJwtException) {
            logger.error("Expired JWT token", e)
        } catch (e: UnsupportedJwtException) {
            logger.error("Unsupported JWT token", e)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string $token is empty", e)
        }
        return null
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(JwtProvider::class.java)
    }
}

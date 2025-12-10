package com.princess.student_activity.helpers

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTUtil(@Value("\${jwt.secret}") private val secret: String) {
    private val secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val log = LoggerFactory.getLogger(this::class.java)

    fun generateToken(id: UUID, expiry: Date): String {
        log.debug("Generating token..")
        return Jwts.builder()
            .setSubject(id.toString())
            .setIssuedAt(Date())
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
            .also { log.debug("Token generated.") }
    }

    fun generateAccessToken(id: UUID): String {
        val expiry = Date(Date().time + (1000 * 60 * 60))
        return generateToken(id, expiry)
    }

    fun generateRefreshToken(id: UUID): String {
        val expiry = Date(Date().time + (1000 * 60 * 60 * 24 * 7))
        return generateToken(id, expiry)
    }

    fun extractToken(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            null
        }
    }
}
package com.princess.student_activity.helpers

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordManager {
    private val encoder = BCryptPasswordEncoder()

    fun hash(raw: String?) =
        raw?.let { encoder.encode(raw) }
            ?: throw IllegalArgumentException("Password is required.")

    fun isMatch(raw: String, hashed: String): Boolean = encoder.matches(raw, hashed)
}
package com.princess.student_activity.helpers

import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*

@Component("auditorAware")
class AuditorAwareImplementation : AuditorAware<UUID> {

    private val systemUUID = UUID.nameUUIDFromBytes("system-id".toByteArray())

    override fun getCurrentAuditor(): Optional<UUID> {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) return Optional.empty()

        val id = when (authentication.principal) {
            is String -> systemUUID
            else -> authentication.principal as UUID
        }

        return Optional.of(id)
    }
}
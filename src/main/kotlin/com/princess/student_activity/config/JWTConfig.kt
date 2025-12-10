package com.princess.student_activity.config

import com.princess.student_activity.helpers.JWTUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class JWTConfig(private val utils: JWTUtil) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        request.getHeader("Authorization")
            ?.let { auth ->
                if (!auth.startsWith("Bearer ")) return@let

                val token = auth.substring(7)

                utils.extractToken(token)?.let { claims ->
                    val id = claims.subject

                    UsernamePasswordAuthenticationToken(
                        UUID.fromString(id),
                        null,
                        emptyList()
                    )
                        .apply {
                            details = WebAuthenticationDetailsSource().buildDetails(request)
                            SecurityContextHolder.getContext().authentication = this
                        }
                }
            }

        filterChain.doFilter(request, response)
    }
}
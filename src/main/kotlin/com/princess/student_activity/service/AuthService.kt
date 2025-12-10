package com.princess.student_activity.service

import com.princess.student_activity.dto.AuthDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.helpers.InvalidLoginException
import com.princess.student_activity.helpers.JWTUtil
import com.princess.student_activity.helpers.PasswordManager
import com.princess.student_activity.helpers.toStudentResponse
import com.princess.student_activity.repository.StudentRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val repository: StudentRepository,
    private val passwordManager: PasswordManager,
    private val jwtUtil: JWTUtil
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun login(request: AuthDTO, response: HttpServletResponse): StudentDTO {
        log.debug("Finding student email..")
        val student = repository.findByEmail(request.email)
            ?: throw InvalidLoginException("Invalid email or password.")

        log.debug("Checking if password matches..")
        return request.password
            .takeIf { passwordManager.isMatch(it, student.password) }
            ?.let {

                val refreshToken = jwtUtil.generateRefreshToken(student.id!!)
                Cookie("refresh_token", refreshToken).apply {
                    isHttpOnly = true
                    path = "/"
                    maxAge = 60 * 60 * 24 * 7
                    response.addCookie(this)
                }

                student.toStudentResponse(jwtUtil.generateAccessToken(student.id!!))
            } ?: run {
            log.error("Password mismatch.")
            throw InvalidLoginException("Invalid email or password.")
        }
    }

    fun refreshToken(token: String): StudentDTO {
        log.debug("Decoding refresh token..")
        val id = jwtUtil.extractToken(token)?.subject
            ?: throw InvalidLoginException("Invalid refresh token.")

        log.debug("Fetching student details..")
        val student = repository.findById(UUID.fromString(id))
            .orElseThrow {
                log.error("Student does not exist!")
                InvalidLoginException("Student does not exist.")
            }

        log.debug("Generating new access token..")
        return student.toStudentResponse(jwtUtil.generateAccessToken(student.id!!))
    }

    fun clearToken(response: HttpServletResponse) {
        log.debug("Clearing refresh token cookie..")
        Cookie("refresh_token", null).apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
            response.addCookie(this)
        }
    }
}
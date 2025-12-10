package com.princess.student_activity.controller

import com.princess.student_activity.dto.AuthDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.service.AuthService
import com.princess.student_activity.service.StudentService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
@RequestMapping("/auth")
class AuthController(private val service: AuthService, private val studentService: StudentService) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthDTO,
        response: HttpServletResponse
    ): StudentDTO {
        log.info("Running POST /login method.")
        return service.login(request, response)
            .also { log.info("Student logged in.") }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: StudentDTO
    ): StudentDTO {
        log.info("Running POST /signup method.")
        return studentService.create(request)
            .also { log.info("Student signed up.") }
    }

    @PostMapping("/refresh")
    fun refreshToken(@CookieValue("refresh_token") token: String): StudentDTO {
        log.info("Running POST /auth/refresh method.")
        return service.refreshToken(token)
            .also { log.info("Access token refreshed.") }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    fun clearToken(response: HttpServletResponse) {
        log.info("Running DELETE /auth method.")
        service.clearToken(response)
            .also { log.info("Student token cleared.") }
    }
}
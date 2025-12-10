package com.princess.student_activity.service

import com.princess.student_activity.dto.AuthDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.helpers.InvalidLoginException
import com.princess.student_activity.helpers.JWTUtil
import com.princess.student_activity.helpers.PasswordManager
import com.princess.student_activity.helpers.toStudentResponse
import com.princess.student_activity.model.StudentEntity
import com.princess.student_activity.repository.StudentRepository
import io.jsonwebtoken.Claims
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.web.MockHttpServletResponse
import java.util.*
import kotlin.apply
import kotlin.collections.firstOrNull
import kotlin.jvm.java
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private val repository = mockk<StudentRepository>(relaxed = true)
    private val passwordManager = mockk<PasswordManager>(relaxed = true)
    private val jwtUtil = spyk(JWTUtil("ThisIsAVeryLongSecretKeyForTesting"))
    private val servlet = mockk<HttpServletResponse>()
    private val authService = AuthService(repository, passwordManager, jwtUtil)

    private val id = UUID.randomUUID()
    private val requestDto = AuthDTO(email = "admin@email.com", password = "password")
    private val tokenExpiry = Date(System.currentTimeMillis() + (1000 * 60 * 60))

    @Test
    fun `login() should pass with the right credentials`() {
        val student = StudentEntity(UUID.randomUUID(), "some", "user", "some@email.com", "somePassword")
        val expected = student.toStudentResponse().copy(token = "someToken") 
            
        every { repository.findByEmail(any()) } returns student
        every { passwordManager.isMatch(any(), any()) } returns true
        every { jwtUtil.generateAccessToken(any()) } returns expected.token!!
        every { servlet.addCookie(any()) } returns Unit
        
        val result = authService.login(requestDto, servlet)

        assertEquals(expected, result)
        verify(exactly = 1) { repository.findByEmail(any()) }
        verify(exactly = 1) { passwordManager.isMatch(any(), any()) }
        verify(exactly = 1) { jwtUtil.generateAccessToken(any()) }
    }

    @Test
    fun `login() should throw InvalidLoginException when user does not exist`() {
        val username = "nonexistentUser"

        every { repository.findByEmail(username) } returns null
        every { servlet.addCookie(any()) } returns Unit

        val result = assertFailsWith<InvalidLoginException> { authService.login(requestDto, mockk()) }

        assertEquals("Invalid email or password.", result.message)
        verify(exactly = 1) { repository.findByEmail(any()) }
    }

    @Test
    fun `login() should throw InvalidLoginException when password does not match`() {
        val existingUser = StudentEntity(UUID.randomUUID(), "some", "user", "some@email.com", "somePassword")

        every { repository.findByEmail(any()) } returns existingUser
        every { passwordManager.isMatch(any(), existingUser.password) } returns false
        every { servlet.addCookie(any()) } returns Unit

        val result = assertFailsWith<InvalidLoginException> { authService.login(requestDto, mockk()) }

        assertEquals("Invalid email or password.", result.message)
        verify(exactly = 1) { repository.findByEmail(any()) }
        verify(exactly = 1) { passwordManager.isMatch(any(), any()) }
    }

    @Test
    fun `refreshToken() should decode refresh token and return new access token`() {
        val existingUser = StudentEntity(UUID.randomUUID(), "some", "user", "some@email.com", "somePassword")
        val token = jwtUtil.generateToken(id, tokenExpiry)
        val expectedResponse = existingUser.toStudentResponse("someToken")

        val claims = mock(Claims::class.java)
        `when`(claims.subject).thenReturn(id.toString())

        every { jwtUtil.extractToken(any()) } returns claims
        every { repository.findById(any()) } returns Optional.of(existingUser)
        every { jwtUtil.generateAccessToken(any()) } returns expectedResponse.token!!

        val result = authService.refreshToken(token)

        assertEquals(expectedResponse, result)
        verify(exactly = 1) { jwtUtil.extractToken(any()) }
        verify(exactly = 1) { repository.findById(any()) }
        verify(exactly = 1) { jwtUtil.generateAccessToken(any()) }
    }

    @Test
    fun `refreshToken() should throw InvalidLoginException if refresh token is invalid`() {
        val refreshToken = "invalid.token.here"

        val result = assertFailsWith<InvalidLoginException> { authService.refreshToken(refreshToken) }

        assertEquals("Invalid refresh token.", result.message)
    }

    @Test
    fun `clearToken() should clear refresh_token cookie`() {
        val response = MockHttpServletResponse()
        authService.clearToken(response)

        val cookies = response.cookies
        val cookie = cookies.firstOrNull { it.name == "refresh_token" }
        assertNull(cookie?.value)
        assertNotNull(cookie?.name)
    }
}
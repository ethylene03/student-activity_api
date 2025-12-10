package com.princess.student_activity.service

import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.helpers.*
import com.princess.student_activity.model.StudentEntity
import com.princess.student_activity.repository.StudentRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*
import kotlin.test.assertEquals

class StudentServiceTest {

    private lateinit var repository: StudentRepository
    private lateinit var passwordManager: PasswordManager
    private lateinit var service: StudentService

    @BeforeEach
    fun setUp() {
        repository = mockk()
        passwordManager = mockk()
        service = StudentService(repository, passwordManager)
    }

    @Test
    fun `create should save new student`() {
        val details = StudentDTO(
            firstName = "Jane",
            lastName = "Doe",
            email = "jane@example.com",
            password = "password123"
        )
        val hashedPassword = "hashedPassword"
        val studentEntity = details.copy(password = hashedPassword).createStudentEntity()
        val savedEntity = studentEntity.apply { id = UUID.randomUUID() }

        every { repository.findByEmail("jane@example.com") } returns null
        every { passwordManager.hash("password123") } returns hashedPassword
        every { repository.save(any()) } returns savedEntity

        val result = service.create(details)

        assertEquals("Jane", result.firstName)
        assertEquals("jane@example.com", result.email)
        verify { repository.save(any()) }
    }

    @Test
    fun `create should throw DuplicateKeyException when email exists`() {
        val details = StudentDTO(
            firstName = "Jane",
            lastName = "Doe",
            email = "jane@example.com",
            password = "password123"
        )
        val existingStudent = StudentEntity(id = UUID.randomUUID(), email = "jane@example.com")

        every { repository.findByEmail("jane@example.com") } returns existingStudent

        assertThrows<DuplicateKeyException> {
            service.create(details)
        }
    }

    @Test
    fun `findAll should return paged students`() {
        val pageable = PageRequest.of(0, 10)
        val student = StudentEntity(id = UUID.randomUUID(), firstName = "Jane", email = "jane@example.com")
        val page = PageImpl(listOf(student))

        every { repository.findAll(pageable) } returns page

        val result = service.findAll(pageable)

        val content = result.content as List<Any>
        assertEquals(1, content.size)
        assertEquals("Jane", (content[0] as StudentDTO).firstName)
        verify { repository.findAll(pageable) }
    }

    @Test
    fun `find should return student when exists`() {
        val id = UUID.randomUUID()
        val student = StudentEntity(id = id, firstName = "Jane", email = "jane@example.com")

        every { repository.findById(id) } returns Optional.of(student)

        val result = service.find(id)

        assertEquals("Jane", result.firstName)
    }

    @Test
    fun `find should throw ResourceNotFoundException when student does not exist`() {
        val id = UUID.randomUUID()

        every { repository.findById(id) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            service.find(id)
        }
    }

    @Test
    fun `update should modify student when valid`() {
        val id = UUID.randomUUID()
        val currentStudent = StudentEntity(id = id, firstName = "Jane", email = "jane@example.com", password = "hashed")
        val details =
            StudentDTO(firstName = "Janet", lastName = "Doe", email = "jane@example.com", password = "password123")

        every { repository.findByEmail("jane@example.com") } returns null
        every { repository.findById(id) } returns Optional.of(currentStudent)
        every { passwordManager.isMatch("password123", "hashed") } returns true
        every { repository.save(any()) } returns currentStudent.apply { firstName = "Janet"; lastName = "Doe" }

        val result = service.update(id, details)

        assertEquals("Janet", result.firstName)
        assertEquals("Doe", result.lastName)
        verify { repository.save(any()) }
    }

    @Test
    fun `update should throw DuplicateKeyException when email exists for another student`() {
        val id = UUID.randomUUID()
        val details =
            StudentDTO(firstName = "Janet", lastName = "Doe", email = "jane@example.com", password = "password123")
        val existingStudent = StudentEntity(id = id, email = "jane@example.com")

        every { repository.findByEmail(any()) } returns existingStudent

        assertThrows<DuplicateKeyException> {
            service.update(id, details)
        }
    }

    @Test
    fun `update should throw InvalidCredentialsException when password mismatch`() {
        val id = UUID.randomUUID()
        val currentStudent = StudentEntity(id = id, firstName = "Jane", email = "jane@example.com", password = "hashed")
        val details =
            StudentDTO(firstName = "Janet", lastName = "Doe", email = "jane@example.com", password = "password123")

        every { repository.findByEmail(any()) } returns null
        every { repository.findById(id) } returns Optional.of(currentStudent)
        every { passwordManager.isMatch(any(), any()) } returns false

        assertThrows<InvalidCredentialsException> {
            service.update(id, details)
        }
    }

    @Test
    fun `delete should remove student`() {
        val id = UUID.randomUUID()
        val student = StudentEntity(id = id)

        every { repository.findById(id) } returns Optional.of(student)
        every { repository.deleteById(id) } just Runs

        service.delete(id)

        verify { repository.deleteById(id) }
    }
}

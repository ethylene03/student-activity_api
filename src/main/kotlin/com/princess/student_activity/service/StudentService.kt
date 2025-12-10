package com.princess.student_activity.service

import com.princess.student_activity.dto.PageDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.helpers.*
import com.princess.student_activity.model.StudentEntity
import com.princess.student_activity.repository.StudentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class StudentService(
    private val repository: StudentRepository, 
    private val passwordManager: PasswordManager
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun create(details: StudentDTO): StudentDTO {
        log.debug("Checking if email is unique..")
        repository.findByEmail(details.email)
            ?.let {
                log.error("Email already exists.")
                throw DuplicateKeyException("Email already exists.")
            }

        log.debug("Creating student..")
        val student = details.copy(password = passwordManager.hash(details.password))
            .createStudentEntity()

        return repository.save(student).toStudentResponse()
    }

    fun findAll(pageable: Pageable): PageDTO {
        log.debug("Fetching all students..")
        return repository.findAll(pageable)
            .toPageDTO(StudentEntity::toStudentResponse)
    }

    fun find(id: UUID): StudentDTO {
        log.debug("Fetching student..")
        return repository.findById(id)
            .orElseThrow {
                log.error("Student Id not found.")
                throw ResourceNotFoundException("Student does not exist.")
            }.toStudentResponse()
    }

    fun update(id: UUID, details: StudentDTO): StudentDTO {
        log.debug("Checking if email is unique..")
        repository.findByEmail(details.email)
            ?.takeIf { it.id == id }
            ?.let {
                log.error("Email already exists.")
                throw DuplicateKeyException("Email already exists.")
            }

        log.debug("Finding student by given ID..")
        val currentStudent = repository.findById(id)
            .orElseThrow {
                log.error("Student not found.")
                ResourceNotFoundException("Student not found.")
            }

        log.debug("Checking if password matches..")
        details.password?.takeUnless { passwordManager.isMatch(it, currentStudent.password) }
            ?.run {
                log.error("Credentials is incorrect.")
                throw InvalidCredentialsException("Given credentials is incorrect.")
            }

        log.debug("Updating student..")
        return currentStudent.apply {
            firstName = details.firstName
            lastName = details.lastName
            email = details.email
        }.run { repository.save(this) }.toStudentResponse()
    }

    fun delete(id: UUID) {
        log.debug("Checking if ID exists..")
        find(id)

        log.debug("Deleting data..")
        repository.deleteById(id)
    }
}
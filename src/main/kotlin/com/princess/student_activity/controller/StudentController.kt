package com.princess.student_activity.controller

import com.princess.student_activity.dto.PageDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.service.StudentService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@Validated
@RestController
@RequestMapping("/students")
class StudentController(private val service: StudentService) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun create(@Valid @RequestBody details: StudentDTO): StudentDTO {
        log.info("Running POST /students method.")
        return service.create(details).also { log.info("Student created.") }
    }

    @GetMapping
    fun findAll(pageable: Pageable): PageDTO {
        log.info("Running GET /students method.")

        return service.findAll(pageable).also { log.info("Students fetched.") }
    }

    @GetMapping("/{id}")
    fun find(@PathVariable("id") id: UUID): StudentDTO {
        log.info("Running GET /students/{id} method.")
        return service.find(id).also { log.info("Student fetched.") }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable("id") id: UUID, @Valid @RequestBody details: StudentDTO): StudentDTO {
        log.info("Running PUT /students/{id} method.")
        return service.update(id, details).also { log.info("Student updated.") }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") id: UUID) {
        log.info("Running DELETE /students/{id} method.")
        return service.delete(id).also { log.info("Student deleted.") }
    }
}
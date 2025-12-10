package com.princess.student_activity.controller

import com.princess.student_activity.dto.ActivityDTO
import com.princess.student_activity.dto.PageDTO
import com.princess.student_activity.service.ActivityService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@Validated
@RestController
@RequestMapping("/activities")
class ActivityController(private val service: ActivityService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun create(@RequestBody @Valid details: ActivityDTO, @AuthenticationPrincipal studentId: UUID): ActivityDTO {
        log.info("Running POST /activities method.")
        return service.create(details, studentId).also { log.info("Activity created.") }
    }

    @GetMapping
    fun findAll(
        @AuthenticationPrincipal studentId: UUID,
        pageable: Pageable,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) date: LocalDate?
    ): PageDTO {
        log.info("Running GET /activities method.")
        return service.findAll(studentId, pageable, query, date).also { log.info("Activities fetched.") }
    }

    @GetMapping("/{id}")
    fun find(@PathVariable("id") activityId: UUID, @AuthenticationPrincipal studentId: UUID): ActivityDTO {
        log.info("Running GET /activities/{id} method.")
        return service.find(activityId, studentId).also { log.info("Activity fetched.") }
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable("id") activityId: UUID,
        @Valid @RequestBody details: ActivityDTO,
        @AuthenticationPrincipal studentId: UUID
    ): ActivityDTO {
        log.info("Running PUT /activities/{id} method.")
        return service.update(activityId, details, studentId).also { log.info("Activity updated.") }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable("id") activityId: UUID, @AuthenticationPrincipal studentId: UUID) {
        log.info("Running DELETE /activities/{id} method.")
        service.delete(activityId, studentId).also { log.info("Activity deleted.") }
    }
}
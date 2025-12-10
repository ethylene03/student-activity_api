package com.princess.student_activity.helpers

import com.princess.student_activity.dto.ActivityDTO
import com.princess.student_activity.dto.PageDTO
import com.princess.student_activity.dto.StudentDTO
import com.princess.student_activity.model.ActivityEntity
import com.princess.student_activity.model.ActivityTypeEntity
import com.princess.student_activity.model.StudentEntity
import org.springframework.data.domain.Page

fun StudentEntity.toStudentResponse(token: String? = null): StudentDTO = StudentDTO(
    id = this.id ?: throw IllegalArgumentException("Student ID is required."),
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    token = token
)

fun StudentDTO.createStudentEntity(): StudentEntity = StudentEntity(
    firstName = this.firstName,
    lastName = this.lastName,
    email = this.email,
    password = this.password ?: throw kotlin.IllegalArgumentException("Password is required."),
)

fun <T : Any> Page<T>.toPageDTO(responseMapper: (T) -> Any): PageDTO = PageDTO(
    content = this.content.map { responseMapper(it) },
    empty = this.isEmpty,
    first = this.isFirst,
    last = this.isLast,
    number = this.number,
    numberOfElements = this.numberOfElements,
    size = this.size,
    totalElements = this.totalElements,
    totalPages = this.totalPages
)

fun ActivityEntity.toActivityResponse(): ActivityDTO = ActivityDTO(
    id = this.id,
    activity = this.activity?.type ?: throw IllegalArgumentException("Activity type is required."),
    description = this.description,
    timestamp = this.timestamp
)

fun ActivityDTO.createActivityEntity(activity: ActivityTypeEntity, student: StudentEntity): ActivityEntity =
    ActivityEntity(
        student = student,
        activity = activity,
        description = this.description,
        timestamp = this.timestamp
    )
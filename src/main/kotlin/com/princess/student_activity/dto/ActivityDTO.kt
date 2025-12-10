package com.princess.student_activity.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime
import java.util.*

data class ActivityDTO(
    val id: UUID? = null,
    @field:NotBlank(message = "Activity type is required.")
    val activity: String,
    val description: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

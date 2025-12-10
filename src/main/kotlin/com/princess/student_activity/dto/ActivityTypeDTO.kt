package com.princess.student_activity.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class ActivityTypeDTO(
    val id: UUID? = null,
    @field:NotBlank(message = "Activity type is required.")
    val type: String,
)

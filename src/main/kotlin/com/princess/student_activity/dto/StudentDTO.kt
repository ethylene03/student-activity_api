package com.princess.student_activity.dto

import jakarta.validation.constraints.NotBlank
import java.util.*

data class StudentDTO(
    val id: UUID? = null,
    @field:NotBlank(message = "Email is required.")
    val firstName: String,
    @field:NotBlank(message = "Email is required.")
    val lastName: String,
    @field:NotBlank(message = "Email is required.")
    val email: String,
    val password: String? = null,
    val token: String? = null
)

package com.princess.student_activity.dto

import jakarta.validation.constraints.NotBlank

data class AuthDTO(
    @field:NotBlank(message = "Email is required.")
    val email: String,
    @field:NotBlank(message = "Email is required.")
    val password: String,
)

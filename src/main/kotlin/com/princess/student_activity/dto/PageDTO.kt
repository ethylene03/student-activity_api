package com.princess.student_activity.dto

data class PageDTO(
    val content: Any,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val numberOfElements: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
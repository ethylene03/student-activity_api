package com.princess.student_activity.dto

import java.time.LocalDate
import java.util.*

data class DashboardDTO(
    val studentId: UUID,
    val totalCount: Long,
    val todayCount: Long,
    val dailyAverage: Double,
    val countPerActivity: List<ActivityCount>,
    val countPerDay: List<DailyCount>
)

data class ActivityCount(
    val activity: String,
    val count: Long
)

data class DailyCount(
    val date: LocalDate,
    val count: Long
)

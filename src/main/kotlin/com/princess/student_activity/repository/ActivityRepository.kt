package com.princess.student_activity.repository

import com.princess.student_activity.dto.DailyCount
import com.princess.student_activity.model.ActivityEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ActivityRepository : JpaRepository<ActivityEntity, UUID>, JpaSpecificationExecutor<ActivityEntity> {
    fun countByStudentId(studentId: UUID): Long
    fun countByTimestampBetweenAndStudentId(start: LocalDateTime, end: LocalDateTime, studentId: UUID): Long

    @Query(
        value = """
        SELECT AVG(cnt)::double precision AS avg_count 
        FROM (
            SELECT DATE(timestamp) AS day, COUNT(*) AS cnt
            FROM activities
            WHERE student_id = :studentId
            GROUP BY DATE(timestamp)
        ) AS daily_counts
    """,
        nativeQuery = true
    )
    fun getDailyAverage(studentId: UUID): Double

    @Query(
        """
    SELECT a.activity.type, COUNT(a)
    FROM ActivityEntity a
    WHERE a.student.id = :studentId
    GROUP BY a.activity.type
"""
    )
    fun countByActivityType(studentId: UUID): List<Array<Any>>

    @Query(
        value = """
        SELECT 
            DATE(a.timestamp) AS day,
            COUNT(*) AS count
        FROM activities a
        WHERE a.student_id = :studentId
        GROUP BY DATE(a.timestamp)
        ORDER BY DATE(a.timestamp)
    """,
        nativeQuery = true
    )
    fun countByDay(studentId: UUID): List<Array<Any>>
}
package com.princess.student_activity.repository

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
        WITH date_series AS (
            SELECT generate_series(
                (SELECT MIN(timestamp)::date FROM activities WHERE student_id = :studentId),
                (SELECT MAX(timestamp)::date FROM activities WHERE student_id = :studentId),
                interval '1 day'
            ) AS day
        )
        SELECT 
            ds.day,
            COUNT(a.id) AS count
        FROM date_series ds
        LEFT JOIN activities a
            ON a.student_id = :studentId
            AND DATE(a.timestamp) = ds.day
        GROUP BY ds.day
        ORDER BY ds.day;
    """,
        nativeQuery = true
    )
    fun countByDay(studentId: UUID): List<Array<Any>>
}
package com.princess.student_activity.service

import com.princess.student_activity.dto.ActivityDTO
import com.princess.student_activity.helpers.UnauthorizedUserException
import com.princess.student_activity.model.ActivityEntity
import com.princess.student_activity.model.ActivityTypeEntity
import com.princess.student_activity.model.StudentEntity
import com.princess.student_activity.repository.ActivityRepository
import com.princess.student_activity.repository.ActivityTypeRepository
import com.princess.student_activity.repository.StudentRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class ActivityServiceTest {

    private lateinit var activityRepository: ActivityRepository
    private lateinit var typeRepository: ActivityTypeRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var service: ActivityService

    @BeforeEach
    fun setUp() {
        activityRepository = mockk()
        typeRepository = mockk()
        studentRepository = mockk()
        service = ActivityService(activityRepository, typeRepository, studentRepository)
    }

    @Test
    fun `create should save new activity`() {
        val studentId = UUID.randomUUID()
        val details = ActivityDTO(
            activity = "Running",
            description = "Morning run",
            timestamp = LocalDateTime.now()
        )
        val student = StudentEntity(id = studentId)
        val activityType = ActivityTypeEntity(type = "Running")
        val savedActivity = ActivityEntity(
            id = UUID.randomUUID(),
            activity = activityType,
            student = student,
            description = "Morning run",
            timestamp = details.timestamp
        )

        every { typeRepository.findByType("Running") } returns null
        every { typeRepository.save(any()) } returns activityType
        every { studentRepository.findById(studentId) } returns Optional.of(student)
        every { activityRepository.save(any()) } returns savedActivity

        val result = service.create(details, studentId)

        assertEquals("Running", result.activity)
        assertEquals("Morning run", result.description)
        verify { typeRepository.save(any()) }
        verify { activityRepository.save(any()) }
    }

    @Test
    fun `findAll should return paged activities`() {
        val studentId = UUID.randomUUID()
        val pageable = PageRequest.of(0, 10)
        val activityType = ActivityTypeEntity(type = "Running")
        val activity = ActivityEntity(
            id = UUID.randomUUID(),
            activity = activityType,
            student = StudentEntity(studentId),
            description = "Desc",
            timestamp = LocalDateTime.now()
        )
        val page = PageImpl(listOf(activity))

        every { activityRepository.findAll(any<Specification<ActivityEntity>>(), any<Pageable>()) } returns page

        val result = service.findAll(studentId, pageable, null, null)

        val content = result.content as List<Any>
        assertEquals(1, content.size)
        assertEquals("Running", (content[0] as ActivityDTO).activity)
        verify { activityRepository.findAll(any<Specification<ActivityEntity>>(), any<Pageable>()) }
    }

    @Test
    fun `find should return activity when authorized`() {
        val studentId = UUID.randomUUID()
        val activityId = UUID.randomUUID()
        val activityType = ActivityTypeEntity(type = "Running")
        val activity = ActivityEntity(id = activityId, activity = activityType, student = StudentEntity(studentId))

        every { activityRepository.findById(activityId) } returns Optional.of(activity)

        val result = service.find(activityId, studentId)

        assertEquals(activityId, result.id)
    }

    @Test
    fun `find should throw exception when unauthorized`() {
        val studentId = UUID.randomUUID()
        val otherStudentId = UUID.randomUUID()
        val activityId = UUID.randomUUID()
        val activityType = ActivityTypeEntity(type = "Running")
        val activity = ActivityEntity(id = activityId, activity = activityType, student = StudentEntity(otherStudentId))

        every { activityRepository.findById(activityId) } returns Optional.of(activity)

        assertThrows<UnauthorizedUserException> {
            service.find(activityId, studentId)
        }
    }

    @Test
    fun `update should modify activity when authorized`() {
        val studentId = UUID.randomUUID()
        val activityId = UUID.randomUUID()
        val details =
            ActivityDTO(activity = "Swimming", description = "Afternoon swim", timestamp = LocalDateTime.now())
        val student = StudentEntity(id = studentId)
        val oldActivityType = ActivityTypeEntity(type = "Running")
        val activity = ActivityEntity(
            id = activityId,
            activity = oldActivityType,
            student = student,
            description = "Old desc",
            timestamp = details.timestamp
        )

        every { activityRepository.findById(activityId) } returns Optional.of(activity)
        every { typeRepository.findByType("Swimming") } returns null
        every { typeRepository.save(any()) } returns ActivityTypeEntity(type = "Swimming")
        every { activityRepository.save(any()) } returns activity.apply { description = "Afternoon swim" }

        val result = service.update(activityId, details, studentId)

        assertEquals("Afternoon swim", result.description)
        verify { activityRepository.save(any()) }
    }

    @Test
    fun `delete should remove activity`() {
        val studentId = UUID.randomUUID()
        val activityId = UUID.randomUUID()
        val activityType = ActivityTypeEntity(type = "Running")
        val activity = ActivityEntity(id = activityId, activity = activityType, student = StudentEntity(studentId))

        every { activityRepository.findById(activityId) } returns Optional.of(activity)
        every { activityRepository.deleteById(activityId) } just Runs

        service.delete(activityId, studentId)

        verify { activityRepository.deleteById(activityId) }
    }

    @Test
    fun `dashboard should return correct counts`() {
        val studentId = UUID.randomUUID()
        every { activityRepository.countByStudentId(studentId) } returns 5
        every { activityRepository.countByTimestampBetweenAndStudentId(any(), any(), eq(studentId)) } returns 2
        every { activityRepository.getDailyAverage(studentId) } returns 1.5
        every { activityRepository.countByActivityType(studentId) } returns listOf(
            arrayOf("Running", 3),
            arrayOf("Swimming", 2)
        )
        every { activityRepository.countByDay(studentId) } returns listOf(
            arrayOf("2025-12-10", 2),
            arrayOf("2025-12-09", 3)
        )

        val result = service.dashboard(studentId)

        assertEquals(5, result.totalCount)
        assertEquals(2, result.todayCount)
        assertEquals(1.5, result.dailyAverage)
        assertEquals(2, result.countPerActivity.size)
        assertEquals(2, result.countPerDay.size)
    }
}

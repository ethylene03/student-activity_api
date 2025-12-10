package com.princess.student_activity.service

import com.princess.student_activity.dto.*
import com.princess.student_activity.helpers.*
import com.princess.student_activity.model.ActivityEntity
import com.princess.student_activity.model.ActivityTypeEntity
import com.princess.student_activity.model.StudentEntity
import com.princess.student_activity.repository.ActivityRepository
import com.princess.student_activity.repository.ActivityTypeRepository
import com.princess.student_activity.repository.StudentRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class ActivityService(
    private val repository: ActivityRepository,
    private val typeRepository: ActivityTypeRepository,
    private val studentRepository: StudentRepository
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun create(details: ActivityDTO, studentId: UUID): ActivityDTO {
        log.debug("Checking activity type..")
        val activity = typeRepository.findByType(details.activity)
            ?: let {
                log.debug("Creating activity type..")
                typeRepository.save(
                    ActivityTypeEntity(type = details.activity)
                )
            }

        log.debug("Fetching student details..")
        val student = studentRepository.findById(studentId).orElseThrow {
            log.error("Student does not exist.")
            ResourceNotFoundException("Student does not exist.")
        }

        log.debug("Saving activity..")
        return details.createActivityEntity(activity, student)
            .let { repository.save(it) }.toActivityResponse()
    }

    fun findAll(studentId: UUID, pageable: Pageable, query: String?, date: LocalDate?): PageDTO {
        log.debug("Creating specification..")
        var spec = Specification<ActivityEntity> { root, _, cb ->
            val studentJoin = root.join<ActivityEntity, StudentEntity>("student")
            cb.equal(studentJoin.get<UUID>("id"), studentId)
        }.and(buildSpecification(query))

        if (date != null) {
            val dateSpec = Specification<ActivityEntity> { root, _, cb ->
                val startOfDay = date.atStartOfDay()
                val endOfDay = date.plusDays(1).atStartOfDay()
                cb.between(root.get("timestamp"), startOfDay, endOfDay)
            }
            spec = spec.and(dateSpec)
        }

        log.debug("Creating custom sorting..")
        val customPageable = PageRequest.of(
            pageable.pageNumber,
            pageable.pageSize,
            Sort.by(
                pageable.sort.map { order ->
                    when (order.property) {
                        "activity" -> Sort.Order(order.direction, "activity.type")
                        else -> order
                    }
                }.toList()
            )
        )

        log.debug("Fetching all student's activities..")
        return repository.findAll(spec, customPageable)
            .toPageDTO(ActivityEntity::toActivityResponse)
    }

    fun find(activityId: UUID, userId: UUID): ActivityDTO {
        log.debug("Finding activity..")
        return repository.findById(activityId).orElseThrow {
            log.error("Activity does not exist.")
            ResourceNotFoundException("Activity does not exist.")
        }.takeIf { it.student?.id == userId }
            ?.toActivityResponse()
            ?: let {
                log.error("Student is not authorized to access this activity.")
                throw UnauthorizedUserException("Student is not authorized to access this activity.")
            }
    }

    fun update(activityId: UUID, details: ActivityDTO, studentId: UUID): ActivityDTO {
        log.debug("Checking if activity exists..")
        val activity = repository.findById(activityId).orElseThrow {
            log.error("Activity does not exist.")
            ResourceNotFoundException("Activity does not exist.")
        }

        log.debug("Checking if student is the activity owner..")
        if (activity.student?.id != studentId) {
            log.error("Student is not authorized to edit this activity.")
            throw UnauthorizedUserException("Student is not authorized to edit this activity.")
        }

        log.debug("Fetching activity type..")
        val type = typeRepository.findByType(details.activity)
            ?: let {
                log.debug("Creating activity type..")
                typeRepository.save(
                    ActivityTypeEntity(type = details.activity)
                )
            }

        log.debug("Updating activity..")
        return activity.apply {
            this.activity = type
            description = details.description
            timestamp = details.timestamp
        }.let { repository.save(it) }.toActivityResponse()
    }

    fun delete(activityId: UUID, studentId: UUID) {
        find(activityId, studentId)

        log.debug("Deleting activity..")
        repository.deleteById(activityId)
    }

    fun dashboard(studentId: UUID): DashboardDTO {
        log.debug("Fetching numerical summary..")
        val totalCount = repository.countByStudentId(studentId)
        val today = LocalDate.now().atStartOfDay()
        val todayCount = repository.countByTimestampBetweenAndStudentId(
            today,
            today.plusDays(1),
            studentId
        )
        val dailyAverage = repository.getDailyAverage(studentId)

        log.debug("Getting activities per activity type..")
        val countPerActivity = repository.countByActivityType(studentId).map { row ->
            ActivityCount(
                activity = row[0] as String,
                count = (row[1] as Number).toLong()
            )
        }

        log.debug("Getting activities per day..")
        val countPerDay = repository.countByDay(studentId).map { row ->
            DailyCount(
                date = LocalDate.parse(row[0].toString().substring(0, 10)),
                count = (row[1] as Number).toLong()
            )
        }

        return DashboardDTO(
            studentId,
            totalCount,
            todayCount,
            dailyAverage,
            countPerActivity,
            countPerDay
        )
    }
}
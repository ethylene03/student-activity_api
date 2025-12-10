package com.princess.student_activity.repository

import com.princess.student_activity.model.ActivityTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ActivityTypeRepository : JpaRepository<ActivityTypeEntity, UUID>,
    JpaSpecificationExecutor<ActivityTypeEntity> {
    fun findByType(type: String): ActivityTypeEntity?
}
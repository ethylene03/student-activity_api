package com.princess.student_activity.repository

import com.princess.student_activity.model.ActivityEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ActivityRepository : JpaRepository<ActivityEntity, UUID>, JpaSpecificationExecutor<ActivityEntity> {
}
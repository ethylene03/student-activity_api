package com.princess.student_activity.repository

import com.princess.student_activity.model.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StudentRepository : JpaRepository<StudentEntity, UUID>, JpaSpecificationExecutor<StudentEntity> {
    fun findByEmail(email: String): StudentEntity?
}
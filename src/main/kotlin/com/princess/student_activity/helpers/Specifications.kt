package com.princess.student_activity.helpers

import com.princess.student_activity.model.ActivityEntity
import com.princess.student_activity.model.ActivityTypeEntity
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

fun buildSpecification(value: String?): Specification<ActivityEntity> {
    return Specification { root, _, cb ->
        val predicates = mutableListOf<Predicate>()

        val search = "%${value?.lowercase()}%"

        if (!value.isNullOrBlank()) {
            val activityJoin = root.join<ActivityEntity, ActivityTypeEntity>("activity")

            val activity = cb.like(
                cb.lower(activityJoin.get("type")),
                search
            )
            val description = cb.like(cb.lower(root.get("description")), search)
            predicates.add(cb.or(activity, description))
        }

        cb.and(*predicates.toTypedArray())
    }
}
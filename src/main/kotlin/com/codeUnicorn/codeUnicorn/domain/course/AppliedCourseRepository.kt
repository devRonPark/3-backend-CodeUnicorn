package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AppliedCourseRepository : JpaRepository<AppliedCourse, Int> {
    @Query("select exists(select count(0) from applied_course where user_id = :userId and course_id = :courseId)", nativeQuery = true)
    fun isAlreadyExist(
        @Param("userId") userId: Int,
        @Param("courseId") courseId: Int,
    ): Int
}

package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AppliedCourseRepository : JpaRepository<AppliedCourse, Int> {
    @Query("select count(0) from applied_course where user_id = :userId and course_id = :courseId", nativeQuery = true)
    fun isAlreadyExist(
        @Param("userId") userId: Int,
        @Param("courseId") courseId: Int,
    ): Long?

    @Query("select id, user_id, course_id, created_at from applied_course where user_id = :userId", nativeQuery = true)
    fun findByUserId(
        @Param("userId") userId: Int
    ): MutableList<AppliedCourse?>
}

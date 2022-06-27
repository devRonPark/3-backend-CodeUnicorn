package com.codeUnicorn.codeUnicorn.domain.likeCourse

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface LikeCourseRepository : JpaRepository<LikeCourse, Int> {

    @Transactional
    @Query(value = "select * from like_course where user_id = :userId and course_id = :courseId", nativeQuery = true)
    fun findByLikeCourse(@Param("userId") userId: Int?, @Param("courseId") courseId: Int): LikeCourse?
}

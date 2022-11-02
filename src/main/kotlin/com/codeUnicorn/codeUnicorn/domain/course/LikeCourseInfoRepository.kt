package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LikeCourseInfoRepository : JpaRepository<LikeCourseInfo, Int> {

    @Transactional
    @Query(
        value = "select * from like_course inner join course on like_course.course_id = course.id where deleted_at IS NULL and  user_id = :userId",
        nativeQuery = true
    )
    fun findByLikeCourseList(@Param("userId") userId: Int?): List<LikeCourseInfo>

    @Transactional
    @Query(
        value = "select count(*) from like_course inner join course on like_course.course_id = course.id where deleted_at IS NULL and  user_id = :userId",
        nativeQuery = true
    )
    fun findByLikeCourseCount(@Param("userId") userId: Int?): Int?
}

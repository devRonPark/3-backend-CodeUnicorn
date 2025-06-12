package com.codeUnicorn.codeUnicorn.domain.course

import com.codeUnicorn.codeUnicorn.domain.likeCourse.DeleteLikeCourse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface LikeCourseDeleteRepository : JpaRepository<DeleteLikeCourse, Int> {

    @Transactional
    @Modifying
    @Query(
        value = "update like_course set deleted_at = :updatedAt where user_id = :userId and course_id = :courseId",
        nativeQuery = true
    )
    fun deleteByLikeCourse(
        @Param("updatedAt") updatedAt: LocalDateTime,
        @Param("userId") userId: Int?,
        @Param("courseId") courseId: Int?
    )
}

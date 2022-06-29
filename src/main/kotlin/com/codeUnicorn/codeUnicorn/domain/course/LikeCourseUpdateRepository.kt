package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LikeCourseUpdateRepository : JpaRepository<LikeCourseUpdate, Int> {

    @Transactional
    @Modifying
    @Query(value = "update course set like_count = like_count + 1 where id = :courseId", nativeQuery = true)
    fun likeCountUpdate(@Param("courseId") courseId: Int): Int?
}

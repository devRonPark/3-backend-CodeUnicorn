package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface CourseDetailRepository : JpaRepository<CourseDetail, Int> {

    @Transactional
    @Query(
        value = "select * from course inner join instructor on course.instructor_id = instructor.id  where course.id = :courseId",
        nativeQuery = true
    )
    fun findByCourseDetail(@Param("courseId") courseId: String): CourseDetail?
}

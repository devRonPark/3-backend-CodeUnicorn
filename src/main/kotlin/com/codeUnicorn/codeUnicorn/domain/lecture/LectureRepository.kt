package com.codeUnicorn.codeUnicorn.domain.lecture

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface LectureRepository : JpaRepository<LectureDetailInfo, Int> {
    @Transactional
    @Query(
        value = "select * from lecture inner join section on lecture.section_id = section.id where lecture.course_id = :courseId and lecture.id = :lectureId",
        nativeQuery = true
    )
    fun findByLectureInfo(
        @Param("courseId") courseId: String,
        @Param("lectureId") lectureId: String
    ): LectureDetailInfo
}

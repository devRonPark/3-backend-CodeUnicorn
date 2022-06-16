package com.codeUnicorn.codeUnicorn.domain.course

import org.springframework.data.jpa.repository.JpaRepository

interface CurriculumInfoRepository : JpaRepository<SectionInfo, Int> {
    fun findByCourseId(courseId: Int): List<SectionInfo>?
}

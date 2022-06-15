package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.domain.course.CourseInfo
import com.codeUnicorn.codeUnicorn.domain.course.CourseInfoRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class CourseService {
    @Autowired
    private lateinit var courseRepository: CourseInfoRepository

    // 코스 정보 조회
    fun getCourseList(category: String, paging: Int): List<CourseInfo>? {

        var courseInfoInDb: List<CourseInfo>? = null
        var courseCount: Int = 0

        if (category == "전체") {
            courseInfoInDb = courseRepository.findByAllCourse(paging) ?: return null
        } else {
            courseInfoInDb = courseRepository.findByCourse(category, paging) ?: return null
        }

        return courseInfoInDb
    }

    fun getCourseCount(category: String): Int {
        var courseCount = 0

        if (category == "전체") {
            courseCount = courseRepository.findByAllCourseCount()
        } else {
            courseCount = courseRepository.findByCourseCount(category)
        }

        return courseCount
    }
}

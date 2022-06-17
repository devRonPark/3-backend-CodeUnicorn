package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.course.CourseDetail
import com.codeUnicorn.codeUnicorn.domain.course.CourseDetailRepository
import com.codeUnicorn.codeUnicorn.domain.course.CourseInfo
import com.codeUnicorn.codeUnicorn.domain.course.CourseInfoRepository
import com.codeUnicorn.codeUnicorn.domain.course.CurriculumInfoRepository
import com.codeUnicorn.codeUnicorn.domain.course.SectionInfo
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureDetailInfo
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureRepository
import com.codeUnicorn.codeUnicorn.exception.CurriculumNotExistException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class CourseService {
    @Autowired
    private lateinit var courseRepository: CourseInfoRepository

    @Autowired
    private lateinit var courseDetailRepository: CourseDetailRepository

    @Autowired
    private lateinit var lectureRepository: LectureRepository

    @Autowired
    private lateinit var curriculumInfoRepository: CurriculumInfoRepository

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

    // 코스 전체 개수 조회
    fun getCourseCount(category: String): Int {
        var courseCount = 0

        if (category == "전체") {
            courseCount = courseRepository.findByAllCourseCount()
        } else {
            courseCount = courseRepository.findByCourseCount(category)
        }

        return courseCount
    }

    // 코스 상세 정보 조회
    fun getCourseDetail(courseId: String): CourseDetail {

        var courseDetailInfo: CourseDetail

        courseDetailInfo = courseDetailRepository.findByCourseDetail(courseId)

        return courseDetailInfo
    }

    // 강의 상세 정보 조회
    fun getLectureInfo(courseId: String, lectureId: String): LectureDetailInfo {

        var lectureInfo: LectureDetailInfo

        lectureInfo = lectureRepository.findByLectureInfo(courseId, lectureId)

        return lectureInfo
    }

    @Throws(CurriculumNotExistException::class)
    fun getCurriculumInfo(courseId: Int): List<SectionInfo> {
        return curriculumInfoRepository.findByCourseId(courseId)
            ?: throw CurriculumNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)
    }
}

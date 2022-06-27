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
import com.codeUnicorn.codeUnicorn.domain.likeCourse.LikeCourseRepository
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.dto.CreateCourseLikeDto
import com.codeUnicorn.codeUnicorn.exception.CurriculumNotExistException
import com.codeUnicorn.codeUnicorn.exception.LikeCourseAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.SessionNotExistException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

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

    @Autowired
    private lateinit var likeCourseRepository: LikeCourseRepository

    // 코스 정보 조회
    fun getCourseList(category: String, paging: Int): List<CourseInfo>? {
        val categoryList = mapOf(
            "frontend" to "프론트엔드",
            "backend" to "백엔드",
            "mobile" to "모바일",
            "language" to "프로그래밍 언어",
            "algorithm" to "알고리즘",
            "database" to "데이터베이스"
        )
        var courseInfoInDb: List<CourseInfo>? = null
        var courseCount: Int = 0

        if (category == "all") {
            courseInfoInDb = courseRepository.findByAllCourse(paging) ?: return null
        } else {
            courseInfoInDb = courseRepository.findByCourse(categoryList[category] ?: "", paging) ?: return null
        }

        return courseInfoInDb
    }

    // 코스 전체 개수 조회
    fun getCourseCount(category: String): Int {
        val categoryList = mapOf(
            "frontend" to "프론트엔드",
            "backend" to "백엔드",
            "mobile" to "모바일",
            "language" to "프로그래밍 언어",
            "algorithm" to "알고리즘",
            "database" to "데이터베이스"
        )
        var courseCount = 0

        if (category == "all") {
            courseCount = courseRepository.findByAllCourseCount()
        } else {
            courseCount = courseRepository.findByCourseCount(categoryList[category] ?: "")
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

    // 모든 강의 정보 조회
    fun getCourseAllList(): List<CourseInfo> {
        return courseRepository.findByAllCourseList()
    }

    // 관심 코스 등록
    fun postCourseLike(request: HttpServletRequest, courseId: Int) {

        val session: HttpSession = request.getSession(false)
            ?: throw SessionNotExistException(ExceptionMessage.SESSION_NOT_EXIST)

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // 세션 속 저장되어 있는 사용자 indexId 변수에 할당
        val userId = userInfoInSession.id

        // 사용자 indexId 기반으로 courseId를 좋아요 했는지 확인
        val likeCourseDB = likeCourseRepository.findByLikeCourse(userId, courseId)

        // 이미 관심 코스로 등록되어 있는 경우 에러 발생
        if (likeCourseDB != null) {
            throw LikeCourseAlreadyExistException(ExceptionMessage.LIKE_COURSE_ALREADY_EXIST)
        }

        val newCourseLikeDto = CreateCourseLikeDto(
            userId,
            courseId
        )

        val likeCourse = newCourseLikeDto.toEntity()
        likeCourseRepository.save(likeCourse)
    }
}

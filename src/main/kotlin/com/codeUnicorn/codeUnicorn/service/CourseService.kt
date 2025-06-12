package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.course.AppliedCourse
import com.codeUnicorn.codeUnicorn.domain.course.AppliedCourseRepository
import com.codeUnicorn.codeUnicorn.domain.course.CourseDetail
import com.codeUnicorn.codeUnicorn.domain.course.CourseDetailRepository
import com.codeUnicorn.codeUnicorn.domain.course.CourseInfo
import com.codeUnicorn.codeUnicorn.domain.course.CourseInfoRepository
import com.codeUnicorn.codeUnicorn.domain.course.CurriculumInfoRepository
import com.codeUnicorn.codeUnicorn.domain.course.LikeCourseDeleteRepository
import com.codeUnicorn.codeUnicorn.domain.course.LikeCourseUpdateRepository
import com.codeUnicorn.codeUnicorn.domain.course.SectionInfo
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureDetailInfo
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureRepository
import com.codeUnicorn.codeUnicorn.domain.likeCourse.LikeCourse
import com.codeUnicorn.codeUnicorn.domain.likeCourse.LikeCourseRepository
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.dto.AppliedCourseDto
import com.codeUnicorn.codeUnicorn.dto.CreateCourseLikeDto
import com.codeUnicorn.codeUnicorn.exception.AppliedCourseAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.LikeCourseAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.MySQLException
import com.codeUnicorn.codeUnicorn.exception.NotFoundException
import com.codeUnicorn.codeUnicorn.exception.RequestParamNotValidException
import com.codeUnicorn.codeUnicorn.exception.UserUnauthorizedException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
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

    @Autowired
    private lateinit var appliedCourseRepository: AppliedCourseRepository

    @Autowired
    private lateinit var likeCourseUpdateRepository: LikeCourseUpdateRepository

    @Autowired
    private lateinit var likeCourseDeleteRepository: LikeCourseDeleteRepository

    // 코스 정보 조회
    @Throws(NotFoundException::class, MySQLException::class)
    fun getCourseList(category: String?, sortBy: String?, page: Int): List<CourseInfo?> {
        val paging = if (page == 1 || page == 0) {
            0
        } else {
            (page - 1) * 9
        }
        val categoryList = mapOf(
            "all" to "전체",
            "frontend" to "프론트엔드",
            "backend" to "백엔드",
            "mobile" to "모바일",
            "language" to "프로그래밍 언어",
            "algorithm" to "알고리즘",
            "database" to "데이터베이스"
        )
        // category 값이 누락되었다면
        if (category == null) {
            throw RequestParamNotValidException(ExceptionMessage.CATEGORY_IS_REQUIRED)
            // sortBy 값이 누락되었다면
        } else if (sortBy == null) {
            throw RequestParamNotValidException(ExceptionMessage.SORTBY_IS_REQUIRED)
        }
        // category는 무조건 categoryList 의 key 값 중 하나여야 한다.
        else if (!categoryList.containsKey(category)) {
            // 잘못된 category 값입니다. 400 예외 발생
            throw RequestParamNotValidException(ExceptionMessage.CATEGORY_IS_INVALID)
            // sortBy 는 무조건 popular 혹은 new 둘 중 하나여야 한다.
        } else if (sortBy != "popular" && sortBy != "new") {
            // 잘못된 sortBy 값입니다. 400 예외 발생
            throw RequestParamNotValidException(ExceptionMessage.SORTBY_IS_INVALID)
        }

        var courseInfoInDb: List<CourseInfo?> = listOf()

        try {
            if (category == "all") {
                if (sortBy == "popular") {
                    courseInfoInDb = courseRepository.findSortedByPopularAllCourseList(paging)
                } else if (sortBy == "new") {
                    courseInfoInDb = courseRepository.findSortedByNewAllCourseList(paging)
                }
            } else if (categoryList.containsKey(category)) {
                if (sortBy == "popular") { // 인기순
                    courseInfoInDb = courseRepository.findSortedByPopularCategorizedCourseList(
                        categoryList[category] ?: "",
                        paging
                    )
                } else if (sortBy == "new") { // 최신순
                    courseInfoInDb = courseRepository.findSortedByNewCategorizedCourseList(
                        categoryList[category] ?: "",
                        paging
                    )
                }
            }
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        // 코스 데이터가 존재하지 않을 경우 404 Not Found
        if (courseInfoInDb.isEmpty()) {
            throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        }
        return courseInfoInDb
    }

    // 코스 전체 개수 조회
    @Throws(MySQLException::class)
    fun getCourseCount(category: String?): Int {
        val categoryList = mapOf(
            "frontend" to "프론트엔드",
            "backend" to "백엔드",
            "mobile" to "모바일",
            "language" to "프로그래밍 언어",
            "algorithm" to "알고리즘",
            "database" to "데이터베이스"
        )

        try {
            val courseCount = if (category == "all") {
                courseRepository.findByAllCourseCount()
            } else {
                courseRepository.findByCourseCount(categoryList[category] ?: "")
            }
            return courseCount
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
    }

    // 코스 상세 정보 조회
    @Throws(NotFoundException::class, MySQLException::class)
    fun getCourseDetail(courseId: String): CourseDetail {
        val courseDetail: CourseDetail?
        try {
            courseDetail = courseDetailRepository.findByCourseDetail(courseId)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        if (courseDetail == null) throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        return courseDetail
    }

    // 강의 상세 정보 조회
    @Throws(NotFoundException::class)
    fun getLectureInfo(courseId: String, lectureId: String): LectureDetailInfo {
        val lectureInfo: LectureDetailInfo?
        try {
            lectureInfo = lectureRepository.findByLectureInfo(courseId, lectureId)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        if (lectureInfo == null) throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        return lectureInfo
    }

    // 코스 커리큘럼 조회
    @Throws(NotFoundException::class, MySQLException::class)
    fun getCurriculumInfo(courseId: Int): List<SectionInfo?> {
        val curriculumInfo: List<SectionInfo?>
        try {
            curriculumInfo = curriculumInfoRepository.findByCourseId(courseId)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        if (curriculumInfo.isEmpty()) {
            throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        }
        return curriculumInfo
    }

    // 모든 강의 정보 조회
    @Throws(MySQLException::class)
    fun getCourseAllList(): List<CourseInfo> {
        try {
            return courseRepository.findByAllCourseList()
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
    }

    // 관심 코스 등록
    @Throws(LikeCourseAlreadyExistException::class)
    fun postCourseLike(request: HttpServletRequest, courseId: Int) {
        val session: HttpSession = request.getSession(false)
            ?: throw NotFoundException(ExceptionMessage.SESSION_NOT_EXIST)

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // 세션 속 저장되어 있는 사용자 indexId 변수에 할당
        val userId = userInfoInSession.id
        val likeCourseDB: LikeCourse?
        try {
            // 사용자 indexId 기반으로 courseId를 좋아요 했는지 확인
            likeCourseDB = likeCourseRepository.findByLikeCourse(userId, courseId)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        // 이미 관심 코스로 등록되어 있는 경우 에러 발생
        if (likeCourseDB != null) {
            throw LikeCourseAlreadyExistException(ExceptionMessage.LIKE_COURSE_ALREADY_EXIST)
        }

        val newCourseLikeDto = CreateCourseLikeDto(
            userId,
            courseId
        )

        try {
            likeCourseUpdateRepository.likeCountUpdate(courseId)

            val likeCourse = newCourseLikeDto.toEntity()
            likeCourseRepository.save(likeCourse)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
    }

    @Throws(AppliedCourseAlreadyExistException::class, UserUnauthorizedException::class)
    fun applyCourse(courseId: Int, request: HttpServletRequest): AppliedCourse? {
        val session: HttpSession? = request.getSession(false)
        log.info { "session: $session" }
        log.info { "session id: ${session?.id ?: "존재하지 않음"}" }
        // 세션 가져오기
        // 세션이 존재하지 않는 경우 예외 발생(401, 로그인한 사용자만 접근 가능)
        if (session == null) {
            throw UserUnauthorizedException(ExceptionMessage.UNAUTHORIZED_USER_CANNOT_ACCESS)
        }

        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // userId 가 세션 안에 존재하지 않는다면 로그인하지 않았다는 의미
        val userId = userInfoInSession.id
            ?: throw UserUnauthorizedException(ExceptionMessage.UNAUTHORIZED_USER_CANNOT_ACCESS)
        val isCourseAlreadyApplied: Boolean
        try {
            // 사용자 코스 신청 정보 전 기존 신청 여부 검증
            isCourseAlreadyApplied = appliedCourseRepository.isAlreadyExist(userId, courseId)?.toInt() == 1
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        if (isCourseAlreadyApplied) {
            throw AppliedCourseAlreadyExistException(ExceptionMessage.APPLIED_COURSE_ALREADY_EXIST)
        }

        try {
            // 사용자 코스 신청 정보 저장
            val appliedCourse = AppliedCourseDto(userId, courseId).toEntity()
            // 사용자 코스 신청 기록 저장
            val savedAppliedCourse = appliedCourseRepository.save(appliedCourse)
            log.info { "신청된 코스 정보 : $savedAppliedCourse" }
            courseRepository.updateUserCount(courseId)
            return savedAppliedCourse
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
    }

    // 관심 교욱 목록 삭제
    @Throws(NotFoundException::class)
    fun deleteLikeCourse(request: HttpServletRequest, courseId: Int) {

        val session: HttpSession = request.getSession(false)
            ?: throw NotFoundException(ExceptionMessage.SESSION_NOT_EXIST)

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // 세션 속 저장되어 있는 사용자 indexId 변수에 할당
        val userId = userInfoInSession.id

        val deletedAt = LocalDateTime.now()
        try {
            likeCourseDeleteRepository.deleteByLikeCourse(deletedAt, userId, courseId)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
    }

    @Throws(MySQLException::class, NotFoundException::class)
    fun getTopThreeCourses(): List<CourseInfo?> {
        var topThreeCourses: List<CourseInfo?>
        try {
            topThreeCourses = courseRepository.findTopThreeCourseList()
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }
        if (topThreeCourses.isEmpty()) throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        return topThreeCourses
    }

    // 코스 검색
    @Throws(NotFoundException::class)
    fun getSearchCourse(keyword: String?): Any {

        // 키워드가 빈값이라면 모든 코스 조회로 연결
        if (keyword == "all") {
            val courseList: List<CourseInfo?>
            val courseCount: Int
            try {
                courseList = courseRepository.findByAllCourseList()
                courseCount = courseRepository.findByAllCourseCount()
            } catch (e: IOException) {
                log.error { "INTERNAL SERVER ERROR: ${e.message}" }
                throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
            }

            val courseInfo = HashMap<String, Any>()
            courseInfo["courses"] = courseList
            courseInfo["courseCount"] = courseCount
            return courseInfo
        }

        // MYSQL like 문 사용하기 위하여 가공
        val searchKeyword = "%$keyword%"
        val courseList: List<CourseInfo?>
        try {
            // 키워드로 코스 정보 조회
            courseList = courseRepository.findSearchCourse(searchKeyword)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }

        // 코스 정보 조회가 빈 값이라면
        if (courseList.isEmpty()) {
            throw NotFoundException(ExceptionMessage.RESOURCE_NOT_EXIST)
        }
        val courseCount: Int
        try {
            // 키워드로 코스 갯수 조회
            courseCount = courseRepository.findSearchCourseCount(searchKeyword)
        } catch (e: IOException) {
            log.error { "INTERNAL SERVER ERROR: ${e.message}" }
            throw MySQLException(ExceptionMessage.INTERNAL_SERVER_ERROR)
        }

        // 데이터 가공
        val courseInfo = HashMap<String, Any>()
        courseInfo["courses"] = courseList
        courseInfo["courseCount"] = courseCount

        return courseInfo
    }
}

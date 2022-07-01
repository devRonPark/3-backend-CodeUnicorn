package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.course.SectionInfo
import com.codeUnicorn.codeUnicorn.service.CourseService
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/courses")
@Validated

class CourseApiController {
    @Autowired
    private lateinit var courseService: CourseService

    // /courses?category=""&page=1
    // 정렬 기준: 인기순(popular), 최신순(new)
    // 코스 정보 조회
    @GetMapping()
    fun getCourseList(
        @RequestParam(required = true) category: String?,
        @RequestParam(value = "sortby", required = true) sortBy: String?,
        @RequestParam(required = true)
        @NotNull(message = "page 값이 누락되었습니다.")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "page는 숫자만 가능합니다.")
        page: String?
    ): ResponseEntity<Any> {
        val courseInfo = HashMap<String, Any>()

        val courseList = courseService.getCourseList(category, sortBy, Integer.parseInt(page))
        val courseListInfo = courseList.toTypedArray()
        courseListInfo.let { courseInfo.put("courses", it) }

        val courseCount = courseService.getCourseCount(category)
        courseInfo["courseCount"] = courseCount

        val successResponse = SuccessResponse(200, courseInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 커리큘럼 정보 조회
    @GetMapping("/{courseId}/curriculum")
    fun getCourseCurriculum(
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val curriculumInfo: List<SectionInfo?> = courseService.getCurriculumInfo(Integer.parseInt(courseId))
        val responseData = mapOf(
            "courseId" to Integer.parseInt(courseId),
            "sections" to curriculumInfo
        )
        val successResponse = SuccessResponse(200, responseData)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 코스 상세 정보 조회
    @GetMapping(path = ["/{courseId}"])
    fun getCourseDetail(
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String
    ): ResponseEntity<Any> {

        val courseDetail = courseService.getCourseDetail(courseId)

        val successResponse = SuccessResponse(200, courseDetail)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 강의 상세 정보 조회
    @GetMapping(path = ["/{courseId}/lectures/{lectureId}"])
    fun getLectureInfo(
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String,
        @PathVariable(value = "lectureId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "lectureId는 숫자만 가능합니다.")
        lectureId: String
    ): ResponseEntity<Any> {
        // lecture 데이터 조회
        val lecture = courseService.getLectureInfo(courseId, lectureId)
        // 데이터 가공해서 리턴해줄 Map 생성
        val lectureInfo: MutableMap<String, Any> = mutableMapOf<String, Any>()
        // 데이터 가공할 Map 생성
        val lectureEdit: MutableMap<String, Any> = mutableMapOf<String, Any>()
        // 데이터 가공
        lecture.id?.let { lectureEdit.put("id", it) }
        lectureEdit["name"] = lecture.name
        lectureEdit["desc"] = lecture.description
        lectureEdit["videoUrl"] = lecture.videoUrl
        lectureEdit["playTime"] = lecture.playTime

        lectureInfo["sectionId"] = lecture.section.sectionId
        lectureInfo["lecture"] = lectureEdit

        val successResponse = SuccessResponse(200, lectureInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 전체 강의 정보 조회
    @GetMapping(path = ["/all"])
    fun getCourseAllList(): ResponseEntity<Any> {

        val category: String = "all"

        val courseInfo = courseService.getCourseAllList()
        val courseCount = courseService.getCourseCount(category)

        val course = HashMap<String, Any>()
        course["courses"] = courseInfo
        course["courseCount"] = courseCount

        return ResponseEntity.status(HttpStatus.OK).body(course)
    }

    @PostMapping(path = ["/{courseId}/likes"])
    fun postCourseLike(
        request: HttpServletRequest,
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String
    ): ResponseEntity<Any> {

        val courseIdToInt = courseId.toInt()

        courseService.postCourseLike(request, courseIdToInt)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(courseId)
    }

    @PostMapping(path = ["/{courseId}/apply"])
    fun applyCourse(
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String,
        request: HttpServletRequest
    ): ResponseEntity<SuccessResponse?> {
        courseService.applyCourse(Integer.parseInt(courseId), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(null)
    }

    @DeleteMapping(path = ["{courseId}/likes"])
    fun deleteCourseLike(
        request: HttpServletRequest,
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String
    ): ResponseEntity<Any> {

        courseService.deleteLikeCourse(request, Integer.parseInt(courseId))

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null)
    }
}

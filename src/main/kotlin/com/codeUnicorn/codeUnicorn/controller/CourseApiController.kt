package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.course.SectionInfo
import com.codeUnicorn.codeUnicorn.service.CourseService
import javax.servlet.http.HttpServletRequest
import javax.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping()
    fun GetCourseList(
        @RequestParam category: String,
        @RequestParam page: Int
    ): ResponseEntity<Any> {
        var paging: Int = 0
        paging = if (page == 1 || page == 0) {
            0
        } else {
            (page - 1) * 9
        }

        val courseList = courseService.getCourseList(category, paging)
        val courseCount = courseService.getCourseCount(category)

        val courseListInfo = courseList?.toTypedArray()

        val courseInfo = HashMap<String, Any>()
        courseListInfo?.let { courseInfo.put("courses", it) }
        courseInfo["courseCount"] = courseCount

        val successResponse = SuccessResponse(200, courseInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    @GetMapping("/{courseId}/curriculum")
    fun getCourseCurriculum(
        @PathVariable(value = "courseId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "courseId는 숫자만 가능합니다.")
        courseId: String,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        val curriculumInfo: List<SectionInfo> = courseService.getCurriculumInfo(Integer.parseInt(courseId))
        val responseData = mapOf(
            "courseId" to Integer.parseInt(courseId),
            "sections" to curriculumInfo
        )
        val successResponse = SuccessResponse(200, responseData)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }
}

package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.service.CourseService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
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

        val courseInfo: MutableMap<String, String?> = mutableMapOf<String, String?>()
        courseInfo["courses"] = courseList.toString()
        courseInfo["courseCount"] = courseCount.toString()

        val successResponse = SuccessResponse(200, courseInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }
}

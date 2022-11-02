package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import com.codeUnicorn.codeUnicorn.domain.lecture.LectureDetailInfo
import com.codeUnicorn.codeUnicorn.exception.LectureNotExistException
import com.codeUnicorn.codeUnicorn.service.LectureService
import javax.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lectures")
class LectureApiController {
    @Autowired
    private lateinit var lectureService: LectureService

    // 전체 강의 목록 조회
    @GetMapping(path = ["/all"])
    fun getLectureList(
        request: HttpServletRequest,
    ): ResponseEntity<MutableList<LectureDetailInfo?>> {
        val lectureList: MutableList<LectureDetailInfo?> = lectureService.getLectureList()
        return ResponseEntity.status(HttpStatus.OK).body(lectureList)
    }

    @ExceptionHandler(LectureNotExistException::class)
    fun handleLectureNotExistException(
        e: LectureNotExistException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.NOT_FOUND.value()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
}

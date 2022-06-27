package com.codeUnicorn.codeUnicorn.exception

import com.codeUnicorn.codeUnicorn.controller.CourseApiController
import com.codeUnicorn.codeUnicorn.domain.Error
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@RestControllerAdvice(basePackageClasses = [CourseApiController::class])
class CourseControllerAdvice {
    // request body, query 에 대한 예외 처리
    @ExceptionHandler(value = [MethodArgumentNotValidException::class, NumberFormatException::class])
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = mutableListOf<Error>()
        e.bindingResult.allErrors.forEach { errorObject ->
            val error = Error().apply {
                // 형 변환
                val field = errorObject as FieldError

                this.field = field.field
                this.message = errorObject.defaultMessage
            }

            errors.add(error)
        }
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value()
            this.message = errors[0].message ?: "요청에 에러가 발생했습니다."
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // request param 에 대한 예외 처리
    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun handleConstraintViolationException(
        e: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = mutableListOf<Error>()

        e.constraintViolations.forEach {
            val error = Error(it.propertyPath.last().name, it.message)
            errors.add(error)
        }
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value()
            this.message = errors[0].message.toString()
            this.method = request.method.toString()
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 커리큘럼 정보가 존재하지 않을 시 발생
    @ExceptionHandler(value = [CurriculumNotExistException::class])
    fun handleResourceNotExistException(
        e: CurriculumNotExistException,
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

    // 코스 관심 목록 추가시 추가가 되어 있는 코스일 경우 발생
    @ExceptionHandler(value = [LikeCourseAlreadyExistException::class])
    fun handleNicknameAlreadyExistException(
        e: LikeCourseAlreadyExistException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.CONFLICT.value()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
            this.timestamp = LocalDateTime.now()
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
}

package com.codeUnicorn.codeUnicorn.exception

import com.codeUnicorn.codeUnicorn.controller.UserApiController
import com.codeUnicorn.codeUnicorn.domain.Error
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

private val log = KotlinLogging.logger {}

@RestControllerAdvice(basePackageClasses = [UserApiController::class])
class GlobalControllerException {
    // request body 프로퍼티에 대한 예외 처리
    @ExceptionHandler(value = [MethodArgumentNotValidException::class, NumberFormatException::class])
    fun methodArgumentNotValidException(
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
        // 2. ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = errors[0].message
            this.method = request.method
            this.path = request.requestURI.toString()
            this.timestamp = LocalDateTime.now()
        }
        // 3. ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // Path Variable 및 Query Param 에 대한 예외 처리
    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun constraintVioloationException(
        e: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = mutableListOf<Error>()

        e.constraintViolations.forEach {
            val error = Error().apply {
                this.field = it.propertyPath.last().name
                this.message = it.message
            }
            errors.add(error)
        }

        // 2. ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = errors[0].message
            this.method = request.method
            this.path = request.requestURI.toString()
            this.timestamp = LocalDateTime.now()
        }

        // 3. ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 파일 업로드 용량 초과 시 발생
    @ExceptionHandler(value = [MaxUploadSizeExceededException::class])
    fun maxUploadSizeExceededException(
        e: MaxUploadSizeExceededException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.info("maxUploadSizeExceededException: $e")

        // 2. ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = "파일의 최대 업로드 용량은 10MB 입니다."
            this.method = request.method
            this.path = request.requestURI.toString()
            this.timestamp = LocalDateTime.now()
        }

        // 3. ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}

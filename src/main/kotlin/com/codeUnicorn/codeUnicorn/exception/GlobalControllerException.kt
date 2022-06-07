package com.codeUnicorn.codeUnicorn.exception

import com.codeUnicorn.codeUnicorn.controller.UserApiController
import com.codeUnicorn.codeUnicorn.domain.user.Error
import com.codeUnicorn.codeUnicorn.domain.user.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice(basePackageClasses = [UserApiController::class])
class GlobalControllerException {
    // request body 프로퍼티에 대한 예외 처리
    @ExceptionHandler(value = [MethodArgumentNotValidException::class])
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
            this.method = request.method
            this.message = "요청에 에러가 발생했습니다."
            this.path = request.requestURI.toString()
            this.timestamp = LocalDateTime.now()
            this.errors = errors[0]
        }
        // 3. ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}

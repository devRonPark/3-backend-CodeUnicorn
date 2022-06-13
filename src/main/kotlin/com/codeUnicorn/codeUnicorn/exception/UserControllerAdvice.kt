package com.codeUnicorn.codeUnicorn.exception

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.controller.UserApiController
import com.codeUnicorn.codeUnicorn.domain.Error
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice(basePackageClasses = [UserApiController::class])
class UserControllerAdvice {
    // request body, request param, query 에 대한 예외 처리
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
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = errors[0].message ?: "요청에 에러가 발생했습니다."
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 사용자 정보가 존재하지 않을 시 발생
    @ExceptionHandler(value = [UserNotExistException::class])
    fun handleUserNotExistException(
        e: UserNotExistException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.NOT_FOUND.value().toString()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // 세션이 존재하지 않을 시 발생
    @ExceptionHandler(value = [SessionNotExistException::class])
    fun handleSessionNotExistException(
        e: SessionNotExistException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.NOT_FOUND.value().toString()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // 파일 존재하지 않을 시 발생
    @ExceptionHandler(value = [MultipartException::class, FileNotExistException::class])
    fun handleFileNotExistException(
        e: FileNotExistException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    // 지원하는 파일 형식에 어긋날 시 발생
    @ExceptionHandler(value = [FileNotSupportedException::class])
    fun handleFileNotSupportedException(
        e: FileNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 파일 업로드 용량 초과 시 발생
    @ExceptionHandler(value = [SizeLimitExceededException::class])
    fun handleMaxUploadSizeExceededException(
        e: SizeLimitExceededException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.BAD_REQUEST.value().toString()
            this.message = ExceptionMessage.FILE_MAX_SIZE_EXCEEDED
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    // 파일 업로드 실패 시 발생
    @ExceptionHandler(value = [FileUploadFailException::class])
    fun handleFileUploadException(
        e: FileUploadFailException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        // Error Response
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.INTERNAL_SERVER_ERROR.value().toString()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

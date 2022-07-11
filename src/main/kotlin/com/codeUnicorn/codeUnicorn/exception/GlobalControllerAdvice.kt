package com.codeUnicorn.codeUnicorn.exception

import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import javax.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    private val log = KotlinLogging.logger {}

    // MySQL 에 대해서 쿼리 작업 중 발생한 예외 처리
    @ExceptionHandler(value = [MySQLException::class])
    fun handleMySQLException(
        e: MySQLException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.info { "MySQL 예외 발생" }
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    // 로그인하지 않은 사용자가 접근할 수 없는 경우에 대한 예외 처리
    @ExceptionHandler(value = [UserUnauthorizedException::class])
    fun handleUserUnauthorizedException(
        e: UserUnauthorizedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.info { "로그인하지 않은 사용자가 접근할 수 없는 리소스" }
        // ErrorResponse
        val errorResponse = ErrorResponse().apply {
            this.status = HttpStatus.UNAUTHORIZED.value()
            this.message = e.message.toString()
            this.method = request.method
            this.path = request.requestURI.toString()
        }
        // ResponseEntity
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(value = [NotFoundException::class])
    fun handleNotFoundException(
        e: NotFoundException,
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

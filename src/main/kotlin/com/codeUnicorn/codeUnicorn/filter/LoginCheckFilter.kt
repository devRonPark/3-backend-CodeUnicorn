package com.codeUnicorn.codeUnicorn.filter

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.exception.UserAccessForbiddenException
import com.codeUnicorn.codeUnicorn.exception.UserUnauthorizedException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.PatternMatchUtils
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

@Component
class LoginCheckFilter : Filter {
    // 인증과 무관하게 항상 접근을 허용하는 요청 Url
    private val whitelist: Array<String> = arrayOf(
        "/", "/users/login",
        "/users/logout",
        "/static/*", "/users/test", "/*"
    )

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpRequest: HttpServletRequest = request as HttpServletRequest
        val requestURI = httpRequest.requestURI
        val httpResponse: HttpServletResponse = response as HttpServletResponse

        try {
            log.info("인증 체크 필터 시작 {}", requestURI)
            if (isLoginCheckPath(requestURI)) {
                log.info("인증 체크 로직 실행 {}", requestURI)
                // 로그인 세션이 존재하면 세션 반환, 세션이 존재하지 않으면 null 값 반환
                val session = httpRequest.getSession(false)

                // 현재 로그인하지 않은 사용자의 접근 시 401 Unauthorized 에러 발생
                if (session?.getAttribute("user") == null) {
                    log.info("미인증 사용자 요청 {}", requestURI)

                    throw UserUnauthorizedException(ExceptionMessage.UNAUTHORIZED_USER_CANNOT_ACCESS)
                }

                val userInfoInSession: User? =
                    jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

                // request URI 에 로그인한 사용자의 userId 가 포함되어 있지 않으면 403 Forbidden 에러 발생
                if (!requestURI.contains(userInfoInSession?.id.toString())) {
                    throw UserAccessForbiddenException(ExceptionMessage.CURRENT_USER_CANNOT_ACCESS)
                }
            }
            chain?.doFilter(request, response)
        } catch (e: UserUnauthorizedException) {
            httpResponse.status = HttpStatus.UNAUTHORIZED.value()
            httpResponse.contentType = MediaType.APPLICATION_JSON_VALUE
            httpResponse.characterEncoding = "UTF-8"
            // ErrorResponse
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.UNAUTHORIZED.value().toString()
                this.message = e.message.toString()
                this.method = request.method
                this.path = request.requestURI.toString()
            }
            jacksonObjectMapper().writeValue(httpResponse.writer, errorResponse)
        } catch (e: UserAccessForbiddenException) {
            httpResponse.status = HttpStatus.FORBIDDEN.value()
            httpResponse.contentType = MediaType.APPLICATION_JSON_VALUE
            httpResponse.characterEncoding = "UTF-8"
            // ErrorResponse
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.FORBIDDEN.value().toString()
                this.message = e.message.toString()
                this.method = request.method
                this.path = request.requestURI.toString()
            }
            jacksonObjectMapper().writeValue(httpResponse.writer, errorResponse)
        }
    }

    // 화이트 리스트의 경우 인증 체크 x
    private fun isLoginCheckPath(requestURI: String): Boolean {
        return !PatternMatchUtils.simpleMatch(whitelist, requestURI)
    }
}

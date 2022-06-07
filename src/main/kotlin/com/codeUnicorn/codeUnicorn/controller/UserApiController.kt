package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.user.ErrorResponse
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.Date
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.validation.Valid

@RestController
@RequestMapping("/users")
class UserApiController { // 의존성 주입
    @Autowired
    private lateinit var userService: UserService

    // 사용자 로그인 API
    @PostMapping(path = ["/login"])
    fun login(
        // Request Body 데이터 유효성 검증
        @Valid @RequestBody requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<SuccessResponse> {
        // 프론트 서버로부터 받아온 사용자 정보(request.body 에 해당)
        println("requestUserDto: $requestUserDto")

        // 각각 회원가입 || 로그인, 사용자 데이터 리턴
        val result: MutableMap<String, Any> = userService.login(requestUserDto, request, response)

        println("타입: ${result["type"]}")
        println("사용자 정보: ${result["data"]}")

        if (result["type"] == "회원가입") {
            val successResponse = SuccessResponse(201, result)
            println("successResponse: $successResponse")
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse)
        }

        val successResponse = SuccessResponse(200, result)
        println("successResponse: $successResponse")
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 사용자 로그아웃 API
    @DeleteMapping(path = ["/logout"])
    fun logout(request: HttpServletRequest): ResponseEntity<Any?> {
        val result: MutableMap<String, String> = userService.logout(request)

        if (result["message"] == "세션이 존재하지 않습니다.") {
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.BAD_REQUEST.value().toString()
                this.method = request.method
                this.message = result["message"]
                this.path = request.requestURI.toString()
                this.timestamp = LocalDateTime.now()
                this.errors = null
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
        }
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/session-info")
    fun sessionInfo(request: HttpServletRequest): ResponseEntity<Any> {
        // 세션이 존재하면 현재 세션 반환, 존재하지 않으면 새로 생성하지 않고 null 반환
        val session: HttpSession? = request.getSession(false)
        // 세션 정보 존재하지 않은 경우 예외 처리
        if (session == null) {
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.NOT_FOUND.value().toString()
                this.method = request.method
                this.message = "세션 정보가 존재하지 않습니다."
                this.path = request.requestURI.toString()
                this.timestamp = LocalDateTime.now()
                this.errors = null
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
        }

        // 세션 정보 조회
        println("sessionId=${session.id}")
        println("maxInactiveInterval=${session.maxInactiveInterval}")
        println("creationTime=${Date(session.creationTime)}")
        println("lastAccessTjme=${Date(session.lastAccessedTime)}")
        println("isNew=${session.isNew}")
        // 세션 정보 제공
        return ResponseEntity.status(HttpStatus.OK).body(session)
    }
}

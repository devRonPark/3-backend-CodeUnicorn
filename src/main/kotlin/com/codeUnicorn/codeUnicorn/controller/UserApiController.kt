package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UpdateNicknameUserDto
import com.codeUnicorn.codeUnicorn.exception.FileNotExistException
import com.codeUnicorn.codeUnicorn.service.S3FileUploadService
import com.codeUnicorn.codeUnicorn.service.UserService
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.validation.Valid
import javax.validation.constraints.Pattern
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/users")
@Validated
class UserApiController { // 의존성 주입
    @Autowired // DI
    private lateinit var userService: UserService

    @Autowired // DI
    private lateinit var s3FileUploadService: S3FileUploadService

    // 사용자 정보 조회 API
    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String
    ): ResponseEntity<Any> {
        val user: User = userService.getUserInfo(Integer.parseInt(userId))
        // 응답해 줄 userInfo 데이터 가공
        val userInfo: MutableMap<String, Any?> = mutableMapOf<String, Any?>()
        userInfo["id"] = user.id.toString()
        userInfo["nickname"] = user.nickname
        userInfo["profilePath"] = user.profilePath
        val successResponse = SuccessResponse(200, userInfo)

        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 사용자 로그인 API
    @PostMapping(path = ["/login"])
    fun login(
        // Request Body 데이터 유효성 검증
        @Valid @RequestBody requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<SuccessResponse> {
        // 각각 회원가입 || 로그인, 사용자 데이터 리턴
        val result: MutableMap<String, Any> = userService.login(requestUserDto, request, response)
        val user: User = result["user"] as User
        // 응답해 줄 userInfo 데이터 가공
        val userInfo: MutableMap<String, Any?> = mutableMapOf<String, Any?>()
        userInfo["id"] = user.id
        userInfo["nickname"] = user.nickname
        userInfo["profilePath"] = user.profilePath
        val successResponse = SuccessResponse(200, userInfo)

        if (result["type"] == "회원가입") {
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse)
        }
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 사용자 로그아웃 API
    @DeleteMapping(path = ["/logout"])
    fun logout(request: HttpServletRequest): ResponseEntity<Any?> {
        userService.logout(request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/session-info")
    fun sessionInfo(request: HttpServletRequest): ResponseEntity<Any> {
        // 세션이 존재하면 현재 세션 반환, 존재하지 않으면 새로 생성하지 않고 null 반환
        val session: HttpSession? = request.getSession(false)
        // 세션 정보 존재하지 않은 경우 예외 처리
        if (session == null) {
            val errorResponse = ErrorResponse().apply {
                this.status = HttpStatus.NOT_FOUND.value()
                this.message = "세션 정보가 존재하지 않습니다."
                this.method = request.method
                this.path = request.requestURI.toString()
                this.timestamp = LocalDateTime.now()
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
        }

        // 세션 정보 제공
        return ResponseEntity.status(HttpStatus.OK).body(session)
    }

    // 사용자 닉네임 업데이트
    @PatchMapping("/{userId}/nickname")
    fun updateNickname(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String?,
        @Valid
        @RequestBody
        updateNicknameUserDto: UpdateNicknameUserDto
    ): ResponseEntity<Any> {
        userService.updateNickname(Integer.parseInt(userId), updateNicknameUserDto.getNickname())

        // 204 응답
        return ResponseEntity.noContent().build()
    }

    // 사용자 프로필 설정
    @PatchMapping("/{userId}/profile")
    fun updateUserProfile(
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String?,
        @RequestParam("image")
        file: MultipartFile?
    ): ResponseEntity<Any> {
        // S3 스토리지에 사용자 프로필 이미지 업로드
        val profilePath = if (file != null) s3FileUploadService.uploadFile(file) else throw FileNotExistException(ExceptionMessage.FILE_NOT_EXIST)
        // 사용자 테이블에 프로필 경로 정보 업데이트
        userService.updateUserProfile(Integer.parseInt(userId), profilePath)
        return ResponseEntity.noContent().build()
    }
}

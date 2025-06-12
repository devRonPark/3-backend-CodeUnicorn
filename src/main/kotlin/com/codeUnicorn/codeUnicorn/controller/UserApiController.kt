package com.codeUnicorn.codeUnicorn.controller

import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.ErrorResponse
import com.codeUnicorn.codeUnicorn.domain.SuccessResponse
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UpdateNicknameUserDto
import com.codeUnicorn.codeUnicorn.exception.NicknameOrProfileRequiredException
import com.codeUnicorn.codeUnicorn.exception.NotSupportedContentTypeException
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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

    // 모든 사용자 정보 조회 API
    @GetMapping(path = ["/all"])
    fun getUserInfoList(): ResponseEntity<MutableList<User?>> {
        val userInfoList = userService.getUserInfoList()
        return ResponseEntity.status(HttpStatus.OK).body(userInfoList)
    }

    // 사용자 정보 조회 API
    @GetMapping(path = ["/{userId}"])
    fun getUserInfo(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String
    ): ResponseEntity<Any> {
        val userInfoFuture = userService.getUserInfo(Integer.parseInt(userId))
        val userInfo = userInfoFuture.join()
        val successResponse = SuccessResponse(200, userInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 사용자 회원가입 API
    @PostMapping(path = ["/signup"])
    fun signup(
        // Request Body 데이터 유효성 검증
        @Valid @RequestBody requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<SuccessResponse> {
        // Request Content-Type에 대한 예외 처리
        if (request.contentType != "application/json") {
            throw NotSupportedContentTypeException(ExceptionMessage.CONTENT_TYPE_NOT_SUPPORTED)
        }
        val userInfo: User = userService.signup(requestUserDto, request, response)
        val successResponse = SuccessResponse(201, userInfo)
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse)
    }

    // 사용자 로그인 API
    @PostMapping(path = ["/login"])
    fun login(
        // Request Body 데이터 유효성 검증
        @Valid @RequestBody requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<SuccessResponse> {
        if (request.contentType != "application/json") {
            throw NotSupportedContentTypeException(ExceptionMessage.CONTENT_TYPE_NOT_SUPPORTED)
        }
        // 로그인한 사용자 데이터 리턴
        val result: Map<String, Any> = userService.login(requestUserDto, request, response)
        val user: User = result["user"] as User
        // 응답해 줄 userInfo 데이터 가공
        val userInfo: Map<String, Any?> = mapOf(
            "id" to user.id,
            "nickname" to user.nickname,
            "profilePath" to user.profilePath,
            "loginSessionId" to result["loginSessionId"]
        )
        val successResponse = SuccessResponse(200, userInfo)

        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 사용자 로그아웃 API
    @DeleteMapping(path = ["/logout"])
    fun logout(request: HttpServletRequest): ResponseEntity<Any?> {
        userService.logout(request)
        return ResponseEntity.noContent().build()
    }

    // 사용자 닉네임 및 프로필 업데이트
    @PostMapping("/{userId}/info")
    fun updateUserInfo(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String,
        @Valid
        @RequestParam(value = "nickname", required = false)
        updateNicknameUserDto: UpdateNicknameUserDto,
        @RequestParam(value = "image", required = false)
        file: MultipartFile?,
        @RequestHeader
        requestHeader: Map<String, Any>
    ): ResponseEntity<Any> {
        val session = request.getSession(false)
        log.info { "리퀘스트 헤더 정보: $requestHeader" }
        log.info { "쿠키 정보: ${request.getHeader("cookie")}" }
        log.info { "세션 정보: ${session.id}" }

        val nickname = updateNicknameUserDto.getNickname()
        // 데이터 검증
        if (nickname == null && file != null && file.isEmpty) {
            throw NicknameOrProfileRequiredException(ExceptionMessage.NICKNAME_OR_PROFILE_REQUIRED)
        }

        // request.body 데이터로 nickname 데이터가 들어올 수도 안 들어올 수도 있다.
        if (nickname != null) {
            // 닉네임 업데이트
            val nicknameUpdateFuture = userService.updateNickname(
                Integer.parseInt(userId),
                nickname
            )
            nicknameUpdateFuture.join()
        }
        if (file != null) {
            // S3 스토리지에 사용자 프로필 이미지 업로드
            val fileUploadFuture = s3FileUploadService.uploadFile(file)
            val fileUploadResult = fileUploadFuture.join()
            // 사용자 테이블에 프로필 경로 정보 업데이트
            val userProfileUpdateFuture = userService.updateUserProfile(Integer.parseInt(userId), fileUploadResult)
            userProfileUpdateFuture.join()
        }
        val userInfoFuture = userService.getUserInfo(Integer.parseInt(userId))
        val userInfo = userInfoFuture.join()
        val successResponse = SuccessResponse(200, userInfo)
        return ResponseEntity.status(HttpStatus.OK).body(successResponse)
    }

    // 회원 탈퇴 API
    @DeleteMapping(path = ["/{userId}"])
    fun deleteUser(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String,
    ): ResponseEntity<SuccessResponse> {
        userService.deleteUser(Integer.parseInt(userId))
        return ResponseEntity.noContent().build()
    }

    // 사용자가 신청한 코스 목록 조회
    @GetMapping(path = ["/{userId}/apply-courses"])
    fun getAppliedCourseList(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String,
    ): ResponseEntity<MutableList<MutableMap<String, Any?>>> {
        val courseList = userService.getAppliedList(Integer.parseInt(userId))
        return ResponseEntity.status(HttpStatus.OK).body(courseList)
    }

    // 사용자의 관심 코스 목록 조회
    @GetMapping(path = ["/{userId}/like-courses"])
    fun getLikeCourseList(
        request: HttpServletRequest,
        @PathVariable(value = "userId")
        @Pattern(regexp = "^(0|[1-9][0-9]*)$", message = "userId는 숫자만 가능합니다.")
        userId: String,
    ): ResponseEntity<Any> {

        val userIdToInt = userId.toInt()

        val likeCourseList = userService.getLikeCourseList(userIdToInt)

        return ResponseEntity.status(HttpStatus.OK).body(likeCourseList)
    }
}

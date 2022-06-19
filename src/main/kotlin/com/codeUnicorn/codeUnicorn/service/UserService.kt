package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.BEHAVIOR_TYPE
import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.constant.PLATFORM_TYPE
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLog
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLogRepository
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.CreateUserDto
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UserAccessLogDto
import com.codeUnicorn.codeUnicorn.exception.NicknameAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.SessionNotExistException
import com.codeUnicorn.codeUnicorn.exception.UserNotExistException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userAccessLogRepository: UserAccessLogRepository

    @Throws(UserNotExistException::class)
    fun getUserInfo(userId: Int): User {
        // 사용자 정보가 존재하지 않을 시 UserNotExistException 예외 발생
        return userRepository.findByIdOrNull(userId)
            ?: throw UserNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)
    }

    // 리턴 값 : { "type": "로그인" || "회원가입", "data": { 사용자 정보 } }
    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun login(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): MutableMap<String, Any> {
        var platformType = ""
        val returnData: MutableMap<String, Any> = mutableMapOf()
        val (email) = requestUserDto

        // 이메일로 사용자 존재 여부 파악
        val userInfoInDb: User? = userRepository.findByEmail(email)

        val user: User

        // 회원가입 처리
        if (userInfoInDb == null) {
            // request body의 email 값에 따라 platformType 결정
            if (email.contains("naver")) {
                platformType = PLATFORM_TYPE.NAVER.toString()
            } else if (email.contains("gmail")) {
                platformType = PLATFORM_TYPE.GOOGLE.toString()
            }

            // 회원가입 사용자의 브라우저 정보 및 IP 주소 정보 수집
            val browserName: String = this.getBrowserInfo(request)
            val ip: String = this.getClientIp(request)
            val defaultProfilePath = "/static/user_default_profile.png"
            // DB 에 저장할 사용자 정보 DTO 생성
            val newUserDto =
                CreateUserDto(
                    requestUserDto.email,
                    requestUserDto.nickname,
                    platformType,
                    defaultProfilePath,
                    ip,
                    browserName
                )
            // 사용자 정보 DTO => 사용자 정보 엔티티로 변환
            user = newUserDto.toEntity()
            userRepository.save(user) // 회원 정보 DB에 저장

            returnData["type"] = "회원가입"
            // 로그인 처리
        } else {

            user = userInfoInDb

            returnData["type"] = "로그인"
        }
        returnData["user"] = user
        // 세션 발급
        // 세션이 존재하지 않는 경우 신규 세션 발급
        val session: HttpSession = request.getSession(true)
        // 사용자 객체 데이터 변환 (Object to JSON string)
        val userInfoForSession = jacksonObjectMapper().writeValueAsString(user)
        // 세션에 로그인 회원 정보 보관
        session.setAttribute("user", userInfoForSession)

        // create a cookie
        val loginCookie = ResponseCookie.from("loginSessionId", session.id)
            .domain("codeunicorn.kr")
            .sameSite("None")
            .secure(true)
            .path("/")
            .maxAge(86400)
            .build()
        response.addHeader("set-cookie", loginCookie.toString())

        // 로그인 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String = this.getClientIp(request) // IPv4 형식의 주소

        // 로그인 로그 쌓기
        val userAccessLog =
            UserAccessLogDto(
                user.id ?: 0,
                BEHAVIOR_TYPE.LOGIN.toString(),
                ip,
                browserName,
                session.id
            )

        val userAccessLogEntity: UserAccessLog = userAccessLog.toEntity()

        userAccessLogRepository.save(userAccessLogEntity)

        return returnData
    }

    @Throws(SessionNotExistException::class)
    fun logout(request: HttpServletRequest) {
        // 세션 가져오기
        // 세션이 존재하지 않는 경우 예외 발생(404, 세션이 존재하지 않음)
        val session: HttpSession = request.getSession(false)
            ?: throw SessionNotExistException(ExceptionMessage.SESSION_NOT_EXIST)

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)
        log.info { "userInfoInSession: $userInfoInSession" }
        // 세션 테이블에 저장된 세션 데이터 삭제됨.
        session.invalidate()

        // 로그아웃 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String = this.getClientIp(request) // IPv4 형식의 주소

        if (userInfoInSession.id != null) {
            // 로그아웃 로그 저장
            val userAccessLog =
                UserAccessLogDto(
                    userInfoInSession.id ?: 0,
                    BEHAVIOR_TYPE.LOGIN.toString(),
                    ip,
                    browserName,
                    session.id
                )
            val userAccessLogEntity: UserAccessLog = userAccessLog.toEntity()
            userAccessLogRepository.save(userAccessLogEntity)
        }
    }

    // 클라이언트 브라우저 정보 가져오기
    fun getBrowserInfo(request: HttpServletRequest): String {
        val userAgent: String = request.getHeader("User-Agent")

        return if (userAgent.indexOf("Trident") > -1) {
            "ie"
        } else if (userAgent.indexOf("Edge") > -1) {
            "edge"
        } else if (userAgent.indexOf("Whale") > -1) {
            "whale"
        } else if (userAgent.indexOf("Opera") > -1 || userAgent.indexOf("OPR") > -1) {
            "opera"
        } else if (userAgent.indexOf("Firefox") > -1) {
            "firefox"
        } else if (userAgent.indexOf("Safari") > -1 && userAgent.indexOf("Chrome") == -1) {
            "safari"
        } else if (userAgent.indexOf("Chrome") > -1) {
            "chrome"
        } else {
            ""
        }
    }

    // 클라이언트 IP 정보 가져오기
    fun getClientIp(request: HttpServletRequest): String {
        val ip: String = if (request.getHeader("X-FORWARDED-FOR") != null) {
            request.getHeader("X-FORWARDED-FOR")
        } else if (request.getHeader("X-FORWARDED-FOR") != null) {
            request.getHeader("X-FORWARDED-FOR")
        } else if (request.getHeader("HTTP_CLIENT_IP") != null) {
            request.getHeader("HTTP_CLIENT_IP")
        } else if (request.remoteAddr != null) {
            request.remoteAddr
        } else {
            ""
        }
        println(">>>> Result : IP Address : $ip")
        return ip
    }

    // 사용자 닉네임 업데이트
    @Transactional
    fun updateNickname(userId: Int, nickname: String): Int? {
        val userWithDuplicatedNickname: User? = userRepository.findByNickname(nickname)

        // 중복되는 닉네임이 이미 존재하는 경우
        if (userWithDuplicatedNickname != null) {
            throw NicknameAlreadyExistException(ExceptionMessage.NICKNAME_ALREADY_EXIST)
        }

        // 중복되는 닉네임이 존재하지 않는 경우 사용자 닉네임 업데이트
        return userRepository.updateNickname(userId, nickname)
    }

    // 사용자 프로필 업데이트
    @Transactional
    fun updateUserProfile(userId: Int, profilePath: String) {
        userRepository.updateProfile(userId, profilePath)
    }
}

package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.BEHAVIOR_TYPE
import com.codeUnicorn.codeUnicorn.constant.PLATFORM_TYPE
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLog
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLogRepository
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.CreateUserDto
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UserAccessLogDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.transaction.Transactional

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userAccessLogRepository: UserAccessLogRepository

    // 리턴 값 : { "type": "로그인" || "회원가입", "data": { 사용자 정보 } }
    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun login(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): MutableMap<String, Any> {
        var platformType: String = ""
        val returnData: MutableMap<String, Any> = mutableMapOf()
        val (email, nickname) = requestUserDto

        // 이메일로 사용자 존재 여부 파악
        val userInfoInDb: User? = userRepository.findByEmail(email)

        var user: User

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

            // DB 에 저장할 사용자 정보 DTO 생성
            val newUserDto =
                CreateUserDto(requestUserDto.email, requestUserDto.nickname, platformType, ip, browserName)
            // 사용자 정보 DTO => 사용자 정보 엔티티로 변환
            user = newUserDto.toEntity()
            userRepository.save(user); // 회원 정보 DB에 저장

            returnData["type"] = "회원가입"
            returnData["data"] = user
            // 로그인 처리
        } else {

            // DB에 저장된 닉네임과 일치여부 확인 후 일치하지 않으면 닉네임 업데이트
            if (userInfoInDb.nickname != nickname) {
                // 닉네임 업데이트
                userRepository.updateNickname(email, nickname)
                // 업데이트 전 조회했던 사용자 엔티티에 업데이트된 닉네임 동기화
                userInfoInDb.nickname = nickname
            }

            user = userInfoInDb

            returnData["type"] = "로그인"
            returnData["user"] = user
        }

        // 세션 발급
        val session: HttpSession = request.getSession(false)
        // 사용자 객체 데이터 변환 (Object to JSON string)
        val userInfoForSession = jacksonObjectMapper().writeValueAsString(user)
        // 세션에 로그인 회원 정보 보관
        session.setAttribute("user", userInfoForSession)

        // 로그인 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String = this.getClientIp(request) // IPv4 형식의 주소

        // 로그인 로그 쌓기
        val userAccessLog: UserAccessLogDto? = if ((userInfoInDb != null) && (userInfoInDb.id != null)) {
            UserAccessLogDto(
                userInfoInDb.id,
                BEHAVIOR_TYPE.LOGIN.toString(),
                ip,
                browserName,
                session.id
            )
        } else {
            null
        }
        val userAccessLogEntity: UserAccessLog? = userAccessLog?.toEntity()

        if (userAccessLogEntity != null) {
            userAccessLogRepository.save(userAccessLogEntity)
        }

        return returnData
    }

    @Transactional
    fun logout(request: HttpServletRequest): MutableMap<String, String> {
        val returnData: MutableMap<String, String> = mutableMapOf()
        // 세션 가져오기
        val session: HttpSession? = request.getSession(false)

        // 세션이 존재하지 않는 경우
        if (session == null) {
            returnData["message"] = "세션이 존재하지 않습니다."
            return returnData
        }

        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)

        // 세션 테이블에 저장된 세션 데이터 삭제됨.
        session.invalidate()

        // 로그아웃 로그 저장
        val userAccessLog: UserAccessLogDto? =
            userInfoInSession.id?.let {
                UserAccessLogDto(
                    it,
                    BEHAVIOR_TYPE.LOGIN.toString(),
                    userInfoInSession.ip,
                    userInfoInSession.browser_type,
                    session.id
                )
            }
        val userAccessLogEntity: UserAccessLog? = userAccessLog?.toEntity()

        if (userAccessLogEntity != null) {
            userAccessLogRepository.save(userAccessLogEntity)
        }

        returnData["message"] = "로그아웃 성공"
        return returnData
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

    fun getSession(request: HttpServletRequest): User? {
        val session: HttpSession = request.getSession(false) ?: return null

        return session.getAttribute("user") as User? ?: return null
    }
}

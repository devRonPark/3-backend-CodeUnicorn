package com.codeUnicorn.codeUnicorn.service

import com.codeUnicorn.codeUnicorn.constant.BEHAVIOR_TYPE
import com.codeUnicorn.codeUnicorn.constant.ExceptionMessage
import com.codeUnicorn.codeUnicorn.domain.course.AppliedCourse
import com.codeUnicorn.codeUnicorn.domain.course.AppliedCourseRepository
import com.codeUnicorn.codeUnicorn.domain.course.LikeCourseInfoRepository
import com.codeUnicorn.codeUnicorn.domain.user.User
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLog
import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLogRepository
import com.codeUnicorn.codeUnicorn.domain.user.UserRepository
import com.codeUnicorn.codeUnicorn.dto.CreateUserDto
import com.codeUnicorn.codeUnicorn.dto.RequestUserDto
import com.codeUnicorn.codeUnicorn.dto.UserAccessLogDto
import com.codeUnicorn.codeUnicorn.exception.AppliedCourseNotExistException
import com.codeUnicorn.codeUnicorn.exception.LikeCourseNotExistException
import com.codeUnicorn.codeUnicorn.exception.MySQLException
import com.codeUnicorn.codeUnicorn.exception.NicknameAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.SessionNotExistException
import com.codeUnicorn.codeUnicorn.exception.UserAlreadyExistException
import com.codeUnicorn.codeUnicorn.exception.UserNotExistException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.io.IOException
import java.time.LocalDateTime
import java.time.Period
import java.util.concurrent.CompletableFuture
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.transaction.Transactional

private val log = KotlinLogging.logger {}

@Service
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userAccessLogRepository: UserAccessLogRepository

    @Autowired
    private lateinit var appliedCourseRepository: AppliedCourseRepository

    @Autowired
    private lateinit var likeCourseInfoRepository: LikeCourseInfoRepository

    @Async
    @Throws(UserNotExistException::class)
    fun getUserInfo(userId: Int): CompletableFuture<User> {
        log.info { "(서비스) : 사용자 정보 조회 쿼리" }
        val userInfoFuture = CompletableFuture.supplyAsync(fun(): User? {
            return userRepository.findByIdOrNull(userId)
        })
        val userInfo = userInfoFuture.join()
        if (userInfo == null || userInfo.deletedAt != null) throw UserNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)
        log.info { "(서비스) : 사용자 정보 조회 완료" }
        return CompletableFuture.completedFuture(userInfo)
    }

    @Throws(UserAlreadyExistException::class)
    fun signup(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): User {
        val (email, nickname) = requestUserDto
        // request body의 email 값에 따라 platformType 결정
        val platformType = email.slice((email.indexOf("@") + 1) until email.indexOf("."))
        // 이메일로 사용자 존재 여부 파악
        if (userRepository.findByEmail(email) != null) {
            throw UserAlreadyExistException(ExceptionMessage.USER_ALREADY_EXIST)
        }

        // 회원가입 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String = this.getClientIp(request)
        val defaultProfilePath = "https://api.codeunicorn.kr/static/user_default_profile.png"
        // DB 에 저장할 사용자 정보 DTO 생성
        val newUserDto =
            CreateUserDto(
                email,
                nickname,
                platformType,
                defaultProfilePath,
                ip,
                browserName
            )
        // 사용자 정보 DTO => 사용자 정보 엔티티로 변환
        val user = newUserDto.toEntity()
        userRepository.save(user) // 회원 정보 DB에 저장
        return user
    }

    // 리턴 값 : { "type": "로그인" || "회원가입", "data": { 사용자 정보 } }
    @Transactional // 트랜잭션 => 실패 => 롤백!
    fun login(
        requestUserDto: RequestUserDto,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Map<String, Any> {
        val (email) = requestUserDto

        // 이메일로 사용자 존재 여부 파악
        val userInfoInDb: User = userRepository.findByEmail(email)
            ?: throw UserNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)

        // 세션 발급
        // 세션이 존재하지 않는 경우 신규 세션 발급
        val session: HttpSession = request.getSession(true)
        // 사용자 객체 데이터 변환 (Object to JSON string)
        val userInfoForSession = jacksonObjectMapper().writeValueAsString(userInfoInDb)
        // 세션에 로그인 회원 정보 보관
        session.setAttribute("user", userInfoForSession)

        // create a cookie
        val loginSessionId = session.id

        val returnData = mapOf(
            "user" to userInfoInDb,
            "loginSessionId" to loginSessionId
        )

        // 로그인 사용자의 브라우저 정보 및 IP 주소 정보 수집
        val browserName: String = this.getBrowserInfo(request)
        val ip: String = this.getClientIp(request) // IPv4 형식의 주소

        // 로그인 로그 쌓기
        val userAccessLog =
            UserAccessLogDto(
                userInfoInDb.id ?: 0,
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
        log.info { "session: $session" }
        // 세션 속 저장되어 있는 사용자 정보 가져오기
        val userInfoInSession: User =
            jacksonObjectMapper().readValue(session.getAttribute("user").toString(), User::class.java)
        log.info { "userInfoInSession: $userInfoInSession" }
        log.info { "id: ${userInfoInSession.id}" }
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
    @Async
    @Transactional
    fun updateNickname(userId: Int, nickname: String): CompletableFuture<Int?> {
        log.info { "(서비스) : 닉네임 중복여부 체크" }
        val nickDuplicatedCheckFuture = CompletableFuture.supplyAsync(fun(): User? {
            log.info { "(서비스) : 닉네임 중복여부 조회 쿼리 날림" }
            return userRepository.findByNickname(nickname)
        })
        val nickDuplicateCheckResult = nickDuplicatedCheckFuture.join()

        if (nickDuplicateCheckResult != null) {
            log.info { "(서비스) : 닉네임 중복됨" }
            throw NicknameAlreadyExistException(ExceptionMessage.NICKNAME_ALREADY_EXIST)
        }
        log.info { "(서비스) : 닉네임 중복여부 체크 통과" }
        val updatedAt = LocalDateTime.now()
        // 중복되는 닉네임이 존재하지 않는 경우 사용자 닉네임 업데이트
        val nicknameUpdateFuture = CompletableFuture.supplyAsync(fun(): Int? {
            log.info { "(서비스) : 닉네임 업데이트 쿼리 날림" }
            return userRepository.updateNickname(userId, nickname, updatedAt)
        })
        val nicknameUpdateResult = nicknameUpdateFuture.join()
        log.info { "(서비스) : 닉네임 업데이트 완료" }
        return CompletableFuture.completedFuture(nicknameUpdateResult)
    }

    // 사용자 프로필 업데이트
    @Async
    @Transactional
    fun updateUserProfile(userId: Int, profilePath: String): CompletableFuture<Int?> {
        val updatedAt = LocalDateTime.now()
        val userProfileUpdateFuture = CompletableFuture.supplyAsync(fun(): Int? {
            log.info { "(서비스) : 프로필 이미지 경로 사용자 정보 업데이트 쿼리 날림" }
            return userRepository.updateProfile(userId, profilePath, updatedAt)
        })
        val userProfileUpdateResult = userProfileUpdateFuture.join()
        log.info { "(서비스) : 프로필 이미지 경로 사용자 정보 업데이트 완료" }
        return CompletableFuture.completedFuture(userProfileUpdateResult)
    }

    // 회원 탈퇴
    @Transactional
    @Throws(MySQLException::class)
    fun deleteUser(userId: Int) {
        val deletedAt = LocalDateTime.now()
        // 사용자의 deleted_at 컬럼 값 업데이트
        try {
            userRepository.deleteUser(userId, deletedAt)
        } catch (e: IOException) {
            throw MySQLException(ExceptionMessage.UPDATE_QUERY_FAIL)
        }
    }

    // 사용자가 신청한 코스 목록 조회
    @Throws(AppliedCourseNotExistException::class, MySQLException::class)
    fun getAppliedList(userId: Int): MutableList<MutableMap<String, Any?>> {
        var courseList: MutableList<AppliedCourse?>
        try {
            courseList = appliedCourseRepository.findByUserId(userId)
        } catch (e: IOException) {
            // 조회 쿼리 요청 중 실패 시
            throw MySQLException(ExceptionMessage.SELECT_QUERY_FAIL)
        }

        // 사용자가 신청한 코스가 존재하지 않는 경우
        if (courseList.size == 0) throw AppliedCourseNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)

        val responseData = mutableListOf<MutableMap<String, Any?>>()
        courseList.forEach(fun(it) {
            val appliedCourseInfo = mutableMapOf<String, Any?>()
            appliedCourseInfo["courseId"] = it?.course?.id
            appliedCourseInfo["name"] = it?.course?.name
            appliedCourseInfo["imagePath"] = it?.course?.imagePath
            appliedCourseInfo["createdAt"] = it?.createdAt?.toLocalDate().toString().replace("-", ". ")
            // dayCount 계산
            val startDateTime = it?.createdAt?.toLocalDate() ?: LocalDateTime.now().toLocalDate()
            val currentDateTime = LocalDateTime.now().toLocalDate()
            val period = Period.between(startDateTime, currentDateTime)
            val years = period.years
            val months = period.months
            val days = period.days
            val dayCount = years * 365 + months * 30 + days
            appliedCourseInfo["dayCount"] = dayCount
            responseData.add(appliedCourseInfo)
        })

        return responseData
    }

    // 사용자의 관심 코스 목록 조회
    fun getLikeCourseList(userId: Int): HashMap<String, Any?> {

        val likeCourseList = likeCourseInfoRepository.findByLikeCourseList(userId)

        if (likeCourseList.isEmpty()) {
            throw LikeCourseNotExistException(ExceptionMessage.RESOURCE_NOT_EXIST)
        }

        val likeCourseCount = likeCourseInfoRepository.findByLikeCourseCount(userId)

        val likeCourseResponse = HashMap<String, Any?>()

        val likeCourseData = mutableListOf<MutableMap<String, Any?>>()

        for (i in likeCourseList.indices) {
            val likeCourse = mutableMapOf<String, Any?>()
            likeCourse["id"] = likeCourseList[i].likeCourseList.id
            likeCourse["category"] = likeCourseList[i].likeCourseList.category
            likeCourse["type"] = likeCourseList[i].likeCourseList.type
            likeCourse["name"] = likeCourseList[i].likeCourseList.name
            likeCourse["imagePath"] = likeCourseList[i].likeCourseList.imagePath
            likeCourse["averageRatings"] = likeCourseList[i].likeCourseList.averageRatings
            likeCourse["ratingsCount"] = likeCourseList[i].likeCourseList.ratingsCount
            likeCourse["userCount"] = likeCourseList[i].likeCourseList.userCount

            likeCourseData.add(likeCourse)
        }

        likeCourseResponse["courses"] = likeCourseData
        likeCourseResponse["courseCount"] = likeCourseCount

        return likeCourseResponse
    }
}

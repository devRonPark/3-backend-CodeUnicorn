package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.user.User
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class RequestUserDto(
    @field: NotBlank(message = "이메일이 누락되었습니다.")
    @field: Pattern(
        regexp = "^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@(gmail|naver)\\.com\$",
        message = "이메일은 반드시 @gmail.com 혹은 @naver.com 를 포함해야 합니다."
    )
    var email: String,
    @field: NotBlank(message = "닉네임이 누락되었습니다.")
    @field: Size(
        min = 1,
        max = 60,
        message = "닉네임은 1 ~ 60자 이어야 합니다.",
    )
    var nickname: String,
)

data class UpdateNicknameUserDto(
    @NotBlank(message = "닉네임이 누락되었습니다.")
    @Size(
        min = 1,
        max = 60,
        message = "닉네임은 1 ~ 60자 이어야 합니다.",
    )
    private val nickname: String
) {
    fun getNickname(): String {
        return nickname
    }
}

data class CreateUserDto(
    private val email: String,
    private val nickname: String?, // 빈 값 허용
    private val platform_type: String,
    private val profile_path: String,
    private val ip: String?,
    private val browser_type: String,
) {
    fun toEntity(): User {
        return User(
            email = this.email,
            nickname = this.nickname,
            platform_type = this.platform_type,
            profile_path = this.profile_path,
            ip = this.ip,
            browser_type = this.browser_type
        )
    }
}

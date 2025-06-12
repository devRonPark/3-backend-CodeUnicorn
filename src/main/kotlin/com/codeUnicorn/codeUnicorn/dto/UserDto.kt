package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.user.User
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class RequestUserDto(
    @field: NotBlank(message = "이메일이 누락되었습니다.")
    @field: Pattern(
        regexp = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+\$",
        message = "이메일 형식에 어긋납니다."
    )
    var email: String,
    @field: Size(
        min = 1,
        max = 60,
        message = "닉네임은 1 ~ 60자 이어야 합니다.",
    )
    var nickname: String?,
)

data class UpdateNicknameUserDto(
    @Size(
        min = 1,
        max = 60,
        message = "닉네임은 1 ~ 60자 이어야 합니다.",
    )
    private val nickname: String?
) {
    fun getNickname(): String? {
        return nickname
    }
}

data class CreateUserDto(
    private val email: String,
    private val nickname: String?, // 빈 값 허용
    private val platformType: String,
    private val profilePath: String,
    private val ip: String?,
    private val browserType: String,
) {
    fun toEntity(): User {
        return User(
            email = this.email,
            nickname = this.nickname,
            platformType = this.platformType,
            profilePath = this.profilePath,
            ip = this.ip,
            browserType = this.browserType
        )
    }
}

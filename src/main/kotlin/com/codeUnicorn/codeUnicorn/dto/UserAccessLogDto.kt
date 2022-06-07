package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.user.UserAccessLog

data class UserAccessLogDto(
    private val userId: Int,
    private val type: String,
    private val ip: String? = null,
    private val browserType: String? = null,
    private val sessionId: String? = null
) {
    fun toEntity(): UserAccessLog {
        return UserAccessLog(
            user_id = this.userId,
            type = this.type,
            ip = this.ip,
            browser_type = this.browserType,
            session_id = this.sessionId
        )
    }
}

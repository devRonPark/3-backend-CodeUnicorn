package com.codeUnicorn.codeUnicorn.domain.user

import java.time.LocalDateTime

data class ErrorResponse(
    var status: String? = null,
    var method: String? = null,
    var message: String? = null,
    var path: String? = null,
    var timestamp: LocalDateTime? = null,
    var errors: Error? = null
)

data class Error(
    var field: String? = null,
    var message: String? = null,
)

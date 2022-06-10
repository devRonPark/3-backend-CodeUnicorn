package com.codeUnicorn.codeUnicorn.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime

data class ErrorResponse(
    var status: String? = null,
    var message: String? = null,
    var method: String? = null,
    var path: String? = null,
    // 직렬화 에러 발생 방지
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    // 서버 응답 전 yyyy-MM-dd HH:mm:ss 형태로 데이터 변환
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    var timestamp: LocalDateTime? = null,
)

data class Error(
    var field: String? = null,
    var message: String? = null,
)

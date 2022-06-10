package com.codeUnicorn.codeUnicorn.domain

// 성공 응답 구조
data class SuccessResponse(
    var status: Int,
    var data: Any? = null
)

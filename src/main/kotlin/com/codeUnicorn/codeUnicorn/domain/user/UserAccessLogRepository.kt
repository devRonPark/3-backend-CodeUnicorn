package com.codeUnicorn.codeUnicorn.domain.user

import org.springframework.data.jpa.repository.JpaRepository

// DB Layer 접근자
// JpaRepository 는 기본적으로 CRUD 쿼리 함수 제공
interface UserAccessLogRepository : JpaRepository<UserAccessLog, Int>

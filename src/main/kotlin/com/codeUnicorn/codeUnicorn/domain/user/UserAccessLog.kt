package com.codeUnicorn.codeUnicorn.domain.user

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "user_access_log") // 엔티티와 매핑할 테이블 지정
data class UserAccessLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    val userId: Int,
    val type: String,
    val ip: String?, // 최초 회원가입 IP
    @Column(name = "browser_type")
    val browserType: String?, // Chrome, Safari, Whale, Firefox, Opera, Edge, Samsung Internet 등
    @Column(name = "session_id")
    val sessionId: String?,
    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @CreationTimestamp // insert 쿼리에 대해 자동으로 생성
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

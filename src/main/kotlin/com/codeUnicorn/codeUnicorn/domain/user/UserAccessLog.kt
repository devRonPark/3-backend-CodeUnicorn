package com.codeUnicorn.codeUnicorn.domain.user

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "User_Access_Log") // 엔티티와 매핑할 테이블 지정
data class UserAccessLog(
    @Id
    val user_id: Int,
    val type: String,
    val ip: String?, // 최초 회원가입 IP
    val browser_type: String?, // Chrome, Safari, Whale, Firefox, Opera, Edge, Samsung Internet 등
    val session_id: String?,
    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @CreationTimestamp // insert 쿼리에 대해 자동으로 생성
    val createDateTime: LocalDateTime = LocalDateTime.now(),
)

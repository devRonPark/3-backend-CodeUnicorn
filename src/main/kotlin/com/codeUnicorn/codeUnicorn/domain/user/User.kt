package com.codeUnicorn.codeUnicorn.domain.user

import com.codeUnicorn.codeUnicorn.constant.ROLE_TYPE
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "user") // 엔티티와 매핑할 테이블 지정
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @JsonIgnore
    var email: String = "",
    var nickname: String? = null, // 네이버 닉네임 규칙 : 한글 1~10자 / 영문 대소문자 2~20자 ,구글 닉네임 규칙 : 6~30자 길이
    @JsonIgnore
    @Column(name = "platform_type")
    var platformType: String = "",
    @JsonIgnore
    var ip: String?, // 최초 회원가입 IP
    @JsonIgnore
    @Column(name = "browser_type")
    var browserType: String = "", // Chrome, Safari, Whale, Firefox, Opera, Edge, Samsung Internet 등
    @Column(name = "profile_path")
    var profilePath: String? = null,
    @JsonIgnore
    @Enumerated(EnumType.STRING) // Enum 값을 문자열로 저장될 수 있도록 선언
    val role: ROLE_TYPE = ROLE_TYPE.USER,
    @JsonIgnore
    @Column(name = "created_at")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @CreationTimestamp // insert 쿼리에 대해 자동으로 생성
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonIgnore
    @Column(name = "updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    var updatedAt: LocalDateTime? = null,
    @JsonIgnore
    @Column(name = "deleted_at")
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    var deletedAt: LocalDateTime? = null,
    @JsonIgnore
    @OneToMany(mappedBy = "userId", fetch = FetchType.LAZY) // User:UserAccessLog = 1:N 관계 설정
    val userAccessLog: List<UserAccessLog>? = null
)

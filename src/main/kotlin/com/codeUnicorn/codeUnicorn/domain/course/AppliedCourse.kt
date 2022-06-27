package com.codeUnicorn.codeUnicorn.domain.course

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "applied_course")
class AppliedCourse(userId: Int, courseId: Int) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @Column(name = "user_id")
    var userId: Int? = userId

    @Column(name = "course_id")
    var courseId: Int? = courseId

    @JsonIgnore
    @Column(name = "created_at")
//    @JsonSerialize(using = LocalDateTimeSerializer::class)
//    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @CreationTimestamp // insert 쿼리에 대해 자동으로 생성
    val createdAt: LocalDateTime = LocalDateTime.now()
}

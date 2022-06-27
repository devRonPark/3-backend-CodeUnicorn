package com.codeUnicorn.codeUnicorn.domain.likeCourse

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "like_course")
data class LikeCourse(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "user_id")
    val userId: Int? = null,
    @Column(name = "course_id")
    val courseId: Int? = null,
)

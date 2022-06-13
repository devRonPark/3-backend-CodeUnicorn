package com.codeUnicorn.codeUnicorn.domain.lecture

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "instructor")
data class Lecture(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    @Column(name = "course_id")
    val courseId: Int,
    @Column(name = "section_id")
    val sectionId: Int,
    @Column(length = 30)
    var name: String,
    @Column(length = 50)
    var description: String,
    @Column(length = 255, name = "video_url")
    var videoUrl: String,
    @Column(name = "play_time")
    var playTime: Int
)

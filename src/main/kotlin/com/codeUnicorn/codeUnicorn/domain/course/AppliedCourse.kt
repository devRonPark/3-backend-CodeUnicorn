package com.codeUnicorn.codeUnicorn.domain.course

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import org.springframework.data.annotation.CreatedDate

@Entity
@Table(name = "applied_course")
class AppliedCourse(userId: Int, courseId: Int) {
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @JsonIgnore
    @Column(name = "user_id")
    var userId: Int? = userId

    @JsonIgnore
    @Column(name = "course_id")
    var courseId: Int? = courseId

    @OneToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", insertable = false, updatable = false)
    val course: CourseInfo? = null

    @Column(name = "created_at")
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Transient
    var dayCount: Int? = null
}

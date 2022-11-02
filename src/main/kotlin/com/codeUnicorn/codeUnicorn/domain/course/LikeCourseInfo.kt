package com.codeUnicorn.codeUnicorn.domain.course

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "like_course")
data class LikeCourseInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    val id: Int? = null,
    @Column(name = "user_id")
    @JsonIgnore
    val userId: Int? = null,
    @Column(name = "course_id")
    val courseId: Int? = null,

    @OneToOne
    @JoinColumn(name = "course_id", referencedColumnName = "id", insertable = false, updatable = false)
    val likeCourseList: LikeCourseList
)

@Entity
@Table(name = "course")
data class LikeCourseList(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    var category: String,
    var type: Int,
    var name: String,
    var description: String,
    @Column(name = "image_path")
    var imagePath: String?,
    @Column(name = "average_ratings")
    var averageRatings: Int,
    @Column(name = "ratings_count")
    var ratingsCount: Int,
    @Column(name = "user_count")
    var userCount: Int,
    @Column(name = "like_count")
    var likeCount: Int
)

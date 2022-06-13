// package com.codeUnicorn.codeUnicorn.domain.instructor
//
// import javax.persistence.Column
// import javax.persistence.Entity
// import javax.persistence.GeneratedValue
// import javax.persistence.GenerationType
// import javax.persistence.Id
// import javax.persistence.Table
//
// @Entity
// @Table(name = "instructor")
// data class Instructor(
//     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//     val id: Int? = null,
//     @Column(length = 30)
//     var name: String,
//     @Column(length = 255, name = "work_experience")
//     var workExperience: String,
//     @Column(length = 255)
//     var introduction: String,
//     @Column(length = 255, name = "profile_path")
//     var profilePath: String
// )

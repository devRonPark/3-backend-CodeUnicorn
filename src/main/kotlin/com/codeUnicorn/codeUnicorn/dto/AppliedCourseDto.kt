package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.course.AppliedCourse

data class AppliedCourseDto(
    val userId: Int,
    val courseId: Int
) {
    fun toEntity(): AppliedCourse {
        return AppliedCourse(
            userId = this.userId,
            courseId = this.courseId
        )
    }
}

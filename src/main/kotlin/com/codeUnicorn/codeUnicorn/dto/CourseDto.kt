// ktlint-disable filename
package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.likeCourse.LikeCourse

data class CreateCourseLikeDto(
    private val userId: Int?,
    private val courseId: Int?,
) {
    fun toEntity(): LikeCourse {
        return LikeCourse(
            userId = this.userId,
            courseId = this.courseId
        )
    }
}

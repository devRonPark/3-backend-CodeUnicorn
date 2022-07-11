package com.codeUnicorn.codeUnicorn.constant

object ExceptionMessage {
    const val FILE_NOT_SUPPORTED = "지원하지 않는 파일 형식입니다."
    const val FILE_MAX_SIZE_EXCEEDED = "파일의 최대 업로드 용량은 10MB입니다."
    const val FILE_UPLOAD_FAIL = "파일 업로드에 실패했습니다."
    const val RESOURCE_NOT_EXIST = "리소스를 찾을 수 없습니다."
    const val CURRENT_USER_CANNOT_ACCESS = "현재 사용자가 접근할 수 없는 리소스입니다."
    const val UNAUTHORIZED_USER_CANNOT_ACCESS = "로그인하지 않은 사용자가 접근할 수 없는 리소스입니다."
    const val SESSION_NOT_EXIST = "세션이 존재하지 않습니다."
    const val USER_ALREADY_EXIST = "이미 회원가입한 사용자입니다."
    const val NICKNAME_ALREADY_EXIST = "이미 존재하는 닉네임입니다."
    const val CONTENT_TYPE_NOT_SUPPORTED = "지원하지 않는 콘텐츠 형식입니다."
    const val NICKNAME_OR_PROFILE_REQUIRED = "수정할 닉네임이나 프로필 이미지가 누락되었습니다."
    const val LIKE_COURSE_ALREADY_EXIST = "이미 관심코스로 등록되었습니다."
    const val INTERNAL_SERVER_ERROR = "내부 서버 에러"
    const val APPLIED_COURSE_ALREADY_EXIST = "이미 수강 중인 코스입니다."
    const val CATEGORY_IS_INVALID = "잘못된 category 값입니다."
    const val SORTBY_IS_INVALID = "잘못된 sortBy 값입니다."
    const val CATEGORY_IS_REQUIRED = "category 값이 누락되었습니다."
    const val SORTBY_IS_REQUIRED = "sortby 값이 누락되었습니다."
}

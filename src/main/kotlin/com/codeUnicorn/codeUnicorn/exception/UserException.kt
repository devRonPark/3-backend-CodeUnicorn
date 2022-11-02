package com.codeUnicorn.codeUnicorn.exception

class UserUnauthorizedException(message: String) : RuntimeException(message)
class UserAccessForbiddenException(message: String) : RuntimeException(message)
class UserAlreadyExistException(message: String) : RuntimeException(message)
class NicknameAlreadyExistException(message: String) : RuntimeException(message)
class NotSupportedContentTypeException(message: String) : RuntimeException(message)
class NicknameOrProfileRequiredException(message: String) : RuntimeException(message)
class LikeCourseAlreadyExistException(message: String) : RuntimeException(message)

package com.codeUnicorn.codeUnicorn.exception

class SessionNotExistException(message: String) : RuntimeException(message)
class UserUnauthorizedException(message: String) : RuntimeException(message)
class UserAccessForbiddenException(message: String) : RuntimeException(message)
class UserNotExistException(message: String) : RuntimeException(message)
class NicknameAlreadyExistException(message: String) : RuntimeException(message)
class NotSupportedContentTypeException(message: String) : RuntimeException(message)

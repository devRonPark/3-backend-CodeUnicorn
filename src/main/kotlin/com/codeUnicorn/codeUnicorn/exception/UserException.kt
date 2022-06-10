package com.codeUnicorn.codeUnicorn.exception

class UserUnauthorizedException(message: String) : RuntimeException(message)

class UserAccessForbiddenException(message: String) : RuntimeException(message)

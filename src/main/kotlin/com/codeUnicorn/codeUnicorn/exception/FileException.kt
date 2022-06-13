package com.codeUnicorn.codeUnicorn.exception

import java.io.IOException

class FileNotExistException(message: String) : RuntimeException(message)
class FileNotSupportedException(message: String) : RuntimeException(message)
class FileUploadFailException(message: String) : IOException(message)

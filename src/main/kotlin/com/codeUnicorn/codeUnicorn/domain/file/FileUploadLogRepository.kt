package com.codeUnicorn.codeUnicorn.domain.file

import org.springframework.data.repository.CrudRepository

interface FileUploadLogRepository : CrudRepository<FileUploadLog, Int>

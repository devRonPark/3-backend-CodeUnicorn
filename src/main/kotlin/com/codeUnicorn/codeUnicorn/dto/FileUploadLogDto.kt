package com.codeUnicorn.codeUnicorn.dto

import com.codeUnicorn.codeUnicorn.domain.file.FileUploadLog

data class FileUploadLogDto(
    val directoryPath: String,
    val name: String,
    val type: String,
    val size: Int
) {
    fun toEntity(): FileUploadLog {
        return FileUploadLog(
            directoryPath = this.directoryPath,
            name = this.name,
            type = this.type,
            size = this.size
        )
    }
}

package com.codeUnicorn.codeUnicorn.domain.file

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "file_upload_log")
class FileUploadLog(directoryPath: String, name: String, type: String, size: Int) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(nullable = false, length = 30)
    var directoryPath: String = directoryPath
    @Column(nullable = false, length = 50)
    var name: String = name
    @Column(nullable = false, length = 5)
    var type: String = type
    @Column(nullable = false)
    var size: Int = size
    @CreationTimestamp
    val uploadedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileUploadLog

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "FileUploadLog" +
            "(id=$id, directory_path='$directoryPath', " +
            "name='$name, type=$type, " +
            "size=$size, uploaded_at=$uploadedAt')"
    }
}

package com.codeUnicorn.codeUnicorn.domain.course

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.AttributeConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "section")
@Convert(converter = PlayTimeConverter::class, attributeName = "totalHours")
class SectionInfo(val name: String, val totalHours: String, val lectureCount: Int) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @Column(name = "course_id")
    @JsonIgnore
    val courseId: Int? = null

    @OneToMany(mappedBy = "sectionId")
    val lectures: MutableList<LectureInfo> = mutableListOf()
}

@Entity
@Table(name = "lecture")
@Convert(converter = PlayTimeConverter::class, attributeName = "playTime")
class LectureInfo(val name: String, val description: String, val videoUrl: String, var playTime: String = "") {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null

    @Column(name = "course_id")
    @JsonIgnore
    val courseId: Int? = null

    @Column(name = "section_id")
    @JsonIgnore
    val sectionId: Int? = null
}

@Converter
class PlayTimeConverter : AttributeConverter<String, Int> {
    override fun convertToDatabaseColumn(attribute: String): Int {
        return attribute.toInt()
    }

    override fun convertToEntityAttribute(dbData: Int): String {
        val hours: Int = dbData / 3600
        // hours 가 10보다 작으면 "0$hours" : "$hours"
        val hoursToString: String = if (hours < 10) "0$hours" else "$hours"
        val minutes = (dbData % 3600) / 60
        // minutes 가 10보다 작으면 "0$minutes" : "$minutes"
        val minutesToString: String = if (minutes < 10) "0$minutes" else "$minutes"
        val seconds = (dbData % 3600) % 60
        val secondsToString: String = if (seconds < 10) "0$seconds" else "$seconds"
        return "$hoursToString:$minutesToString:$secondsToString"
    }
}

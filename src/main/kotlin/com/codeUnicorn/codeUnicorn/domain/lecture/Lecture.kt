package com.codeUnicorn.codeUnicorn.domain.lecture

import com.codeUnicorn.codeUnicorn.domain.course.PlayTimeConverter
import com.codeUnicorn.codeUnicorn.domain.section.SectionDetailInfo
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.AttributeConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Converter
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "lecture")
@Convert(converter = PlayTimeConverter::class, attributeName = "playTime")
data class LectureDetailInfo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,
    var name: String,
    var description: String,
    @Column(name = "dash_url")
    var dashUrl: String,
    @Column(name = "hls_url")
    var hlsUrl: String,
    var playTime: String,

    @JsonIgnore
    @ManyToOne
    val section: SectionDetailInfo
)

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

package com.crstlnz.komikchino.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.crstlnz.komikchino.data.model.DisqusConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Entity(tableName = "chapterDownload", indices = [Index(value = ["id"], unique = true)])
@TypeConverters(DisqusConfigConverter::class)
data class ChapterDownloadItem(
    @PrimaryKey(autoGenerate = false) val id: String = "0",
    var title: String = "",
    var slug: String = "0",
    val mangaId: String = "0",
    val disqusConfig: DisqusConfig? = null
)

enum class DownloadState {
    FAILED, PENDING, SUCCESS
}

@Entity(tableName = "chapterImages", indices = [Index(value = ["id"], unique = false)])
@TypeConverters(DownloadStateConverter::class)
data class ChapterImages(
    val id: String = "0",
    val index: Int = 0,
    @PrimaryKey(autoGenerate = false) val url: String = "",
    val state: DownloadState = DownloadState.PENDING
)

class DownloadStateConverter {
    @TypeConverter
    fun toDownloadState(value: String) = enumValueOf<DownloadState>(value)

    @TypeConverter
    fun fromDownloadState(value: DownloadState) = value.name
}

class DisqusConfigConverter {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @TypeConverter
    fun toJson(disqusConfig: DisqusConfig): String {
        return objectMapper.writeValueAsString(disqusConfig)
    }

    @TypeConverter
    fun fromJson(disqusConfigString: String): DisqusConfig {
        val typeFactory = objectMapper.typeFactory
        return objectMapper.readValue(
            disqusConfigString,
            typeFactory.constructType(DisqusConfig::class.java)
        )
    }
}
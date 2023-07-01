package com.crstlnz.komikchino.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
class StorageItem(
    private val value: String,
    private val date: Date = Date(),
    private val expireTimeInMillis: Long = 3600000L
) {
    fun getValue(): String {
        return value
    }

    fun getDate(): Date {
        return date
    }

    fun isValid(): Boolean {
        return expireTimeInMillis == 0L || (System.currentTimeMillis() - date.time) < expireTimeInMillis
    }
}

data class StorageItemValue<T>(
    val data: T,
    val isValid: Boolean
)

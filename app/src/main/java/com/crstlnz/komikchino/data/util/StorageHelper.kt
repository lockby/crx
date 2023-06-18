package com.crstlnz.komikchino.data.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.crstlnz.komikchino.data.model.StorageItem
import com.crstlnz.komikchino.data.model.StorageItemValue
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException

class StorageHelper<T>(
    private val context: Context,
    private val sharedPreferencesName: String,
    private val objectType: JavaType,
    private val expireTimeInMillis: Long = 3600000L
) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
    }

    fun getContext(): Context {
        return context
    }

    private val objectMapper: ObjectMapper by lazy {
        jacksonObjectMapper()
    }

    fun <T> set(key: String, value: Any?) {
        val storageItem = StorageItem(
            value = objectMapper.writeValueAsString(value),
            expireTimeInMillis = expireTimeInMillis
        )
        val jsonString = objectMapper.writeValueAsString(storageItem)
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    fun <T> getRaw(key: String, type: JavaType? = null): StorageItemValue<T>? {
        val jsonString = sharedPreferences.getString(key, null)
        if (jsonString != null) {
            try {
                val data = objectMapper.readValue(jsonString, StorageItem::class.java)
                return StorageItemValue(
                    objectMapper.readValue(
                        data.getValue(),
                        type.let { type } ?: objectType
                    ), isValid = data.isValid()
                )
            } catch (e: IOException) {
                sharedPreferences.edit().remove(key).apply()
                e.printStackTrace()
            }
        }
        return null
    }

    fun <T> get(key: String, defaultValue: T? = null, type: JavaType? = null): T? {
        val jsonString = sharedPreferences.getString(key, null)
        if (jsonString != null) {
            try {
                val storageItems = objectMapper.readValue(jsonString, StorageItem::class.java)
                if (expireTimeInMillis == 0L || storageItems.isValid()) {
                    return objectMapper.readValue(
                        storageItems.getValue().toString(),
                        type.let { type } ?: objectType)
                }
            } catch (e: IOException) {
                sharedPreferences.edit().remove(key).apply()
                e.printStackTrace()
            }
        }
        return defaultValue
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
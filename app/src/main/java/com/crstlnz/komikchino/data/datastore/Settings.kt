package com.crstlnz.komikchino.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.UpdateState
import com.crstlnz.komikchino.hilt.settings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class Settings @Inject constructor(
    private val context: Context
) {
    private val CHAPTER_SORT = booleanPreferencesKey("chapter_sort")
    private val SERVER = stringPreferencesKey("komik_server")
    private val HOMEPAGE = stringPreferencesKey("homepage")
    private val UPDATE_AVAILABLE = stringPreferencesKey("update_availables")

    val isChapterSortAscending: Flow<Boolean> = context.settings.data.map { preferences ->
        preferences[CHAPTER_SORT] ?: false
    }

    suspend fun setChapterSort(isAscending: Boolean) {
        context.settings.edit { settings ->
            settings[CHAPTER_SORT] = isAscending
        }
    }

    val komikServer: Flow<KomikServer> = context.settings.data.map { preferences ->
        try {
            KomikServer.valueOf(
                preferences[SERVER]?.uppercase() ?: KomikServer.KIRYUU.toString()
            )
        } catch (e: Exception) {
            KomikServer.KIRYUU
        }
    }

    suspend fun getServer(): KomikServer {
        return try {
            KomikServer.valueOf(
                context.settings.data.first()[SERVER]?.uppercase() ?: KomikServer.KIRYUU.toString()
            )
        } catch (e: Exception) {
            KomikServer.KIRYUU
        }
    }

    suspend fun setServer(server: KomikServer) {
        context.settings.edit { settings ->
            settings[SERVER] = server.value
        }
    }

    val homepage: Flow<HomeSections> = context.settings.data.map { preferences ->
        try {
            HomeSections.getByRoute(
                preferences[HOMEPAGE] ?: HomeSections.HOME.route
            ) ?: HomeSections.HOME
        } catch (e: Exception) {
            HomeSections.HOME
        }
    }

    suspend fun getHomepage(): HomeSections {
        return try {
            Log.d("ISINYA", context.settings.data.first()[HOMEPAGE].toString())
            HomeSections.getByRoute(
                context.settings.data.first()[HOMEPAGE] ?: HomeSections.HOME.route
            ) ?: HomeSections.HOME
        } catch (e: Exception) {
            Log.d("GET HOMEPAGE", e.stackTraceToString())
            HomeSections.HOME
        }
    }

    suspend fun setHomepage(homepage: HomeSections) {
        context.settings.edit { settings ->
            settings[HOMEPAGE] = homepage.route
        }
    }

    suspend fun setUpdate(state : UpdateState){
        val mapper = jacksonObjectMapper()
        context.settings.edit { settings ->
            settings[UPDATE_AVAILABLE] = mapper.writeValueAsString(state)
        }
    }

    suspend fun getUpdate(): UpdateState? {
        val mapper = jacksonObjectMapper()
        return try {
            val data = context.settings.data.first()[UPDATE_AVAILABLE]
            if (data != null) {
                return mapper.readValue(data, UpdateState::class.java)
            }
            return null
        } catch (e: Exception) {
            null
        }
    }
}
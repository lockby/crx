package com.crstlnz.komikchino.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crstlnz.komikchino.hilt.settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class KomikServer(val value: String) {
    KIRYUU("kiryuu"), MANGAKATANA("mangakatana")
}

class Settings @Inject constructor(
    private val context: Context
) {
    private val CHAPTER_SORT = booleanPreferencesKey("chapter_sort")
    private val SERVER = stringPreferencesKey("komik_server")

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
}
package com.crstlnz.komikchino.hilt

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.crstlnz.komikchino.data.datastore.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

val Context.settings by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
class DatastoreModules {
    @Provides
    fun provideSettings(@ApplicationContext context: Context): Settings {
        return Settings(context)
    }
}
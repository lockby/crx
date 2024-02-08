package com.crstlnz.komikchino.hilt

import android.content.Context
import android.util.Log
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.api.getScraper
import com.crstlnz.komikchino.data.database.KomikDatabase
import com.crstlnz.komikchino.data.database.repository.MangaDownloadRepository
import com.crstlnz.komikchino.data.firebase.repository.ChapterHistoryRepository
import com.crstlnz.komikchino.data.firebase.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.firebase.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.datastore.Settings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
class AppModules() {

    @Provides
    fun provideHomepage(settings: Settings): HomeSections {
        val server = AppSettings.homepage
        return server ?: runBlocking {
            settings.getHomepage()
        }
    }

    @Provides
    fun provideDatabaseKey(settings: Settings): KomikServer {
        val server = AppSettings.komikServer
            ?: return runBlocking {
                AppSettings.komikServer = settings.getServer()
                AppSettings.komikServer!!
            }
        return server
    }

    @Provides
    fun provideApiClient(
        databaseKey: KomikServer,
        @ApplicationContext context: Context,
    ): ScraperBase {
        return getScraper(databaseKey, context)
    }

    @Provides
    fun provideChapterHistoryRepository(
        databaseKey: KomikServer
    ): ChapterHistoryRepository {
        return ChapterHistoryRepository(databaseKey)
    }

    @Provides
    fun provideKomikHistoryRepository(
        databaseKey: KomikServer
    ): KomikHistoryRepository {
        return KomikHistoryRepository(databaseKey)
    }

    @Provides
    fun provideFavoriteKomikRepository(
        databaseKey: KomikServer
    ): FavoriteKomikRepository {
        return FavoriteKomikRepository(databaseKey)
    }

    @Provides
    fun provideMangaDownloadRepository(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): MangaDownloadRepository {
        val database = KomikDatabase.getInstance(context, databaseKey)
        return MangaDownloadRepository(
            database.getMangaDownloadDao(),
            database.getChapterDownloadDao()
        )
    }
}
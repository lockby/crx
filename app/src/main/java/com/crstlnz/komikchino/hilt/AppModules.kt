package com.crstlnz.komikchino.hilt

import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.api.getScraper
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.api.source.Mangakatana
import com.crstlnz.komikchino.data.api.source.Manhwalist
import com.crstlnz.komikchino.data.api.source.VoidScans
import com.crstlnz.komikchino.data.database.repository.ChapterHistoryRepository
import com.crstlnz.komikchino.data.database.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.datastore.Settings
import com.crstlnz.komikchino.ui.navigations.HomeSections
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
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
        return server ?: runBlocking {
            settings.getServer()
        }
    }

    @Provides
    fun provideApiClient(databaseKey: KomikServer): ScraperBase {
       return getScraper(databaseKey)
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
}
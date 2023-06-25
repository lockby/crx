package com.crstlnz.komikchino.hilt

import android.content.Context
import com.crstlnz.komikchino.config.AppSettings
import com.crstlnz.komikchino.data.api.source.Kiryuu
import com.crstlnz.komikchino.data.api.source.Mangakatana
import com.crstlnz.komikchino.data.api.source.ScraperBase
import com.crstlnz.komikchino.data.database.KomikDatabase
import com.crstlnz.komikchino.data.database.chapterhistory.ChapterHistoryRepository
import com.crstlnz.komikchino.data.database.favorite.FavoriteKomikRepository
import com.crstlnz.komikchino.data.database.komik.KomikHistoryRepository
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.data.datastore.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideDatabaseKey(settings: Settings): KomikServer {
        val server = AppSettings.komikServer
        return server ?: runBlocking {
            settings.getServer()
        }
    }

    @Provides
    fun provideApiClient(databaseKey: KomikServer): ScraperBase {
        return when (databaseKey) {
            KomikServer.KIRYUU -> {
                Kiryuu()
            }

            KomikServer.MANGAKATANA -> {
                Mangakatana()
            }
        }
    }

    @Provides
    fun provideChapterHistoryRepository(
        @ApplicationContext context: Context, databaseKey: KomikServer
    ): ChapterHistoryRepository {
        val database = KomikDatabase.getInstance(context, databaseKey)
        return ChapterHistoryRepository(database.getChapterHistoryDao())
    }

    @Provides
    fun provideKomikHistoryRepository(
        @ApplicationContext context: Context, databaseKey: KomikServer
    ): KomikHistoryRepository {
        val database = KomikDatabase.getInstance(context, databaseKey)
        return KomikHistoryRepository(database.getKomikHistoryDao())
    }

    @Provides
    fun provideFavoriteKomikRepository(
        @ApplicationContext context: Context, databaseKey: KomikServer
    ): FavoriteKomikRepository {
        val database = KomikDatabase.getInstance(context, databaseKey)
        return FavoriteKomikRepository(database.getFavoriteKomikDao())
    }
}
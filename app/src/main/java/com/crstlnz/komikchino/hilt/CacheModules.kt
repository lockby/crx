package com.crstlnz.komikchino.hilt

import android.content.Context
import com.crstlnz.komikchino.data.datastore.KomikServer
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.SearchItem
import com.crstlnz.komikchino.data.util.StorageHelper
import com.fasterxml.jackson.databind.type.TypeFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class CacheModules {
    @Provides
    @Named("homeFragmentCache")
    fun provideHomeCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<HomeData> {
        return StorageHelper(
            context,
            "$databaseKey-CACHE",
            TypeFactory.defaultInstance().constructType(HomeData::class.java)
        )
    }

    @Provides
    @Named("komikCache")
    fun provideKomikCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<KomikDetail> {
        return StorageHelper(
            context,
            "$databaseKey-CACHE",
            TypeFactory.defaultInstance().constructType(KomikDetail::class.java),
            expireTimeInMillis = 1800000L
        )
    }

    @Provides
    @Named("chapterCache")
    fun provideChapterCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<ChapterModel> {
        return StorageHelper(
            context, "$databaseKey-CACHE", TypeFactory.defaultInstance()
                .constructType(ChapterModel::class.java),
            604800000L
        )
    }

    @Provides
    @Named("searchCache")
    fun provideSearchCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<List<SearchItem>> {
        return StorageHelper(
            context, "$databaseKey-CACHE", TypeFactory.defaultInstance()
                .constructParametricType(List::class.java, SearchItem::class.java)
        )
    }
}
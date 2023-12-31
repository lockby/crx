package com.crstlnz.komikchino.hilt

import android.content.Context
import com.crstlnz.komikchino.data.api.KomikServer
import com.crstlnz.komikchino.data.model.Chapter
import com.crstlnz.komikchino.data.model.ChapterData
import com.crstlnz.komikchino.data.model.ChapterScrollPostition
import com.crstlnz.komikchino.data.model.Genre
import com.crstlnz.komikchino.data.model.GenreSearch
import com.crstlnz.komikchino.data.model.GithubModel
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.LatestUpdate
import com.crstlnz.komikchino.data.model.SearchHistoryModel
import com.crstlnz.komikchino.data.model.SearchResult
import com.crstlnz.komikchino.data.util.StorageHelper
import com.fasterxml.jackson.databind.type.TypeFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

enum class Cache {
    SCROLL_POSITION,
    DEFAULT_CACHE,
    LATEST_UPDATES,
    KOMIK,
    CHAPTER,
    SEARCH,
    GENRE_SEARCH,
    UPDATE,
    CHAPTER_LIST,
    GENRE_LIST
}

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
            "$databaseKey-${Cache.DEFAULT_CACHE}",
            TypeFactory.defaultInstance().constructType(HomeData::class.java),
            3600000
        )
    }

    @Provides
    @Named("latestUpdateCache")
    fun provideLatestUpdate(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<List<LatestUpdate>> {
        return StorageHelper(
            context, "$databaseKey-${Cache.LATEST_UPDATES}", TypeFactory.defaultInstance()
                .constructParametricType(List::class.java, LatestUpdate::class.java),
            3600000L
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
            "$databaseKey-${Cache.KOMIK}",
            TypeFactory.defaultInstance().constructType(KomikDetail::class.java),
            expireTimeInMillis = 1800000L,
        )
    }

    @Provides
    @Named("chapterCache")
    fun provideChapterCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<ChapterData> {
        return StorageHelper(
            context, "$databaseKey-${Cache.CHAPTER}", TypeFactory.defaultInstance()
                .constructType(ChapterData::class.java),
            604800000L
        )
    }

    @Provides
    @Named("searchCache")
    fun provideSearchCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<List<SearchResult.ExactMatch>> {
        return StorageHelper(
            context, "$databaseKey-${Cache.SEARCH}", TypeFactory.defaultInstance()
                .constructParametricType(List::class.java, SearchResult.ExactMatch::class.java),
            3600000L
        )
    }

    @Provides
    @Named("genreSearchCache")
    fun provideGenreSearchCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<GenreSearch> {
        return StorageHelper(
            context, "$databaseKey-${Cache.GENRE_SEARCH}", TypeFactory.defaultInstance()
                .constructType(GenreSearch::class.java),
            7200000L
        )
    }

    @Provides
    @Named("chapterScrollPositionCache")
    fun provideChapterScrollPositionCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<ChapterScrollPostition> {
        return StorageHelper(
            context,
            "$databaseKey-${Cache.SCROLL_POSITION}",
            TypeFactory.defaultInstance().constructType(ChapterScrollPostition::class.java),
            0L
        )
    }

    @Provides
    @Named("updateCache")
    fun provideUpdateCache(
        @ApplicationContext context: Context,
    ): StorageHelper<GithubModel> {
        return StorageHelper(
            context,
            Cache.UPDATE.name,
            TypeFactory.defaultInstance().constructType(GithubModel::class.java),
            1000L
        )
    }

    @Provides
    @Named("searchHistoryCache")
    fun provideSearchHistoryCache(
        @ApplicationContext context: Context,
    ): StorageHelper<SearchHistoryModel> {
        return StorageHelper(
            context,
            "SEARCH-HISTORY",
            TypeFactory.defaultInstance().constructType(SearchHistoryModel::class.java),
            60000L
        )
    }

    @Provides
    @Named("chapterListCache")
    fun provideChapterListCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<List<Chapter>> {
        return StorageHelper(
            context,
            "$databaseKey-${Cache.CHAPTER_LIST}",
            TypeFactory.defaultInstance()
                .constructParametricType(List::class.java, Chapter::class.java),
            60000L
        )
    }

    @Provides
    @Named("genreList")
    fun provideGenreListCache(
        @ApplicationContext context: Context,
        databaseKey: KomikServer
    ): StorageHelper<List<Genre>> {
        return StorageHelper(
            context,
            "$databaseKey-${Cache.GENRE_LIST}",
            TypeFactory.defaultInstance()
                .constructParametricType(List::class.java, Genre::class.java),
            86400000L
        )
    }
}
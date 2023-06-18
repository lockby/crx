package com.crstlnz.komikchino.data.util

import android.content.Context
import com.crstlnz.komikchino.data.model.ChapterModel
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.model.SearchItem
import com.fasterxml.jackson.databind.type.TypeFactory

fun komikStorageHelper(context: Context): StorageHelper<KomikDetail> {
    return StorageHelper(
        context, "CACHE", TypeFactory.defaultInstance()
            .constructType(KomikDetail::class.java),
        expireTimeInMillis = 1800000L
    )
}

fun homeStorageHelper(context: Context): StorageHelper<HomeData> {
    return StorageHelper(
        context, "CACHE", TypeFactory.defaultInstance()
            .constructType(HomeData::class.java)
    )
}

fun searchStorageHelper(context: Context): StorageHelper<List<SearchItem>> {
    return StorageHelper(
        context, "CACHE", TypeFactory.defaultInstance()
            .constructParametricType(List::class.java, SearchItem::class.java)
    )
}

fun chapterStorageHelper(context: Context): StorageHelper<ChapterModel> {
    return StorageHelper(
        context, "CACHE", TypeFactory.defaultInstance()
            .constructType(ChapterModel::class.java)
    )
}
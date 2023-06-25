package com.crstlnz.komikchino.data.database.favorite

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_komik", indices = [Index(value = ["id"], unique = true)])
data class FavoriteKomikItem(
    @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true) val data_id: Long = 0L,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "img") var img: String,
    @ColumnInfo(name = "slug") var slug: String,
    @ColumnInfo(name = "description") var description: String = "",
    @ColumnInfo(name = "type") var type: String,
)
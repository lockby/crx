package com.crstlnz.komikchino.ui.screens.home.fragments.bookmark


import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.firebase.repository.FavoriteKomikRepository
import com.crstlnz.komikchino.data.firebase.repository.KomikHistoryRepository
import com.crstlnz.komikchino.data.model.ChapterScrollPostition
import com.crstlnz.komikchino.data.util.StorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val historyRepository: KomikHistoryRepository,
    private val favoriteRepository: FavoriteKomikRepository,
    @Named("chapterScrollPositionCache") private val chapterScrollPosition: StorageHelper<ChapterScrollPostition>,
) : ViewModel() {
    //    private val _histories = MutableStateFlow<List<KomikReadHistory>?>(null)
    val histories = historyRepository.readHistories()
    val favorites = favoriteRepository.getAll()

    init {
//        updateHistories()
    }

//    fun updateHistories() {
//        viewModelScope.launch {
//            val history = historyRepository.getHistories()
//            _histories.update {
//                history
//            }
//        }
//    }

    fun getChapterScrollPosition(mangaId: String, chapterId: String): ChapterScrollPostition? {
        val pos = chapterScrollPosition.get<ChapterScrollPostition>(mangaId)
        return if (pos != null && pos.chapterId == chapterId) pos else null
    }


    private val _editState = mutableStateListOf<String>()
    val editState = _editState
    private val selectableMap = mutableMapOf<String, MutableList<String>>()
    private val selectDataMap = mutableMapOf<String, MutableList<String>>()

    fun getSelected(id: String): MutableList<String> {
        return if (selectableMap.contains(id)) {
            selectableMap[id]!!
        } else {
            val list = mutableStateListOf<String>()
            selectableMap[id] = list
            list
        }
    }

    private fun getSelectData(id: String): MutableList<String> {
        return if (selectDataMap.contains(id)) {
            selectDataMap[id]!!
        } else {
            val list = mutableStateListOf<String>()
            selectDataMap[id] = list
            list
        }
    }

    fun setData(pageId: String, data: List<String>) {
        getSelectData(pageId).clear()
        getSelectData(pageId).addAll(data)
    }

    fun select(pageId: String, id: String) {
        if (getSelected(pageId).contains(id)) {
            getSelected(pageId).remove(id)
            if (getSelected(pageId).isEmpty()) {
                cancelEdit(pageId)
            }
        } else {
            getSelected(pageId).add(id)
        }
    }

    fun edit(pageId: String) {
        _editState.add(pageId)
    }

    fun isAllSelected(pageId: String): Boolean {
        if (getSelectData(pageId).size == 0) return false
        return getSelectData(pageId).all { getSelected(pageId).contains(it) }
    }

    fun selectAll(pageId: String) {
        getSelected(pageId).clear()
        getSelected(pageId).addAll(getSelectData(pageId))
    }

    fun deselectAll(pageId: String) {
        getSelected(pageId).clear()
    }

    fun deleteItem(pageId: String) {
        val items = getSelected(pageId)

        for (item in items) {
            if (pageId == "0") { // recent view
                viewModelScope.launch {
                    historyRepository.delete(item)
                }
            } else if (pageId == "1") { // favorites view
                viewModelScope.launch {
                    favoriteRepository.delete(item)
                }
            }
        }

        cancelEdit(pageId)
    }

    fun cancelEdit(pageId: String) {
        deselectAll(pageId)
        _editState.removeAll(listOf(pageId))
    }

    override fun onCleared() {
        super.onCleared()
        historyRepository.close()
        favoriteRepository.close()
    }
}
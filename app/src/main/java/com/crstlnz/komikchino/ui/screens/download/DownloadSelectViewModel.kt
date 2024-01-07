package com.crstlnz.komikchino.ui.screens.download

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.database.model.ChapterDownloadItem
import com.crstlnz.komikchino.data.database.model.MangaChapterDownload
import com.crstlnz.komikchino.data.database.model.MangaDownloadItem
import com.crstlnz.komikchino.data.database.repository.MangaDownloadRepository
import com.crstlnz.komikchino.data.model.DataState.Idle.getDataOrNull
import com.crstlnz.komikchino.data.model.KomikDetail
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class DownloadSelectViewModel @Inject constructor(
    @Named("komikCache") storage: StorageHelper<KomikDetail>,
    private val mangaDownloadRepository: MangaDownloadRepository,
    savedStateHandle: SavedStateHandle,
    private val api: ScraperBase
) : ScraperViewModel<KomikDetail>(storage) {
    private var slug = "0"
    override var cacheKey = "komik-${slug}"
    val downloadSelect = mutableStateListOf<ChapterDownloadItem>()
    var downloadData: LiveData<MangaChapterDownload>
    fun select(chapter: ChapterDownloadItem) {
        val c = downloadSelect.find {
            (it.id ?: "") === chapter.id
        }
        if (c !== null) {
            downloadSelect.remove(c)
        } else {
            downloadSelect.add(chapter)
        }
    }

    fun clear() {
        downloadSelect.clear()
    }

    fun addDownload() {
        val data = state.value.getDataOrNull() ?: return
        viewModelScope.launch {
            mangaDownloadRepository.addDownload(
                MangaDownloadItem(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    img = data.img,
                    slug = data.slug,
                    type = data.type,
                ),
                downloadSelect.toList()
            )
        }
    }

    fun isAllSelected(): Boolean {
        val data = state.value.getDataOrNull() ?: return false
        val chapters =
            data.chapters.filter { downloadData.value?.chapters?.find { c -> c.id == it.id } == null }
        return chapters.all { downloadSelect.find { c -> it.id == c.id } != null }
    }

    fun selectAll() {
        val data = state.value.getDataOrNull() ?: return
        val chapters =
            data.chapters.filter { downloadData.value?.chapters?.find { c -> c.id == it.id } == null }
        downloadSelect.clear()
        downloadSelect.addAll(chapters.map {
            ChapterDownloadItem(
                id = it.id ?: "",
                slug = it.slug,
                title = it.title,
                mangaId = it.mangaId ?: "0"
            )
        }
        )
    }

    fun deselectAll() {
        downloadSelect.clear()
    }

    fun has(id: String): Boolean {
        return downloadSelect.find {
            it.id === id
        } !== null
    }

    init {
        slug = URLDecoder.decode(savedStateHandle.get<String>("slug").orEmpty(), "UTF-8")
            .ifEmpty { "0" }
        cacheKey = "komik-$slug"
        downloadData = mangaDownloadRepository.readDownloadDataBySlug(slug)
        load(force = false, isManual = false)
    }

    override suspend fun fetchData(): KomikDetail {
        return api.getDetailKomik(slug)
    }
}
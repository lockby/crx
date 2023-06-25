package com.crstlnz.komikchino.ui.screens.home.fragments.home

import com.crstlnz.komikchino.data.api.source.ScraperBase
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeFragmenViewModel @Inject constructor(
    @Named("homeFragmentCache") storage: StorageHelper<HomeData>,
    private val api: ScraperBase
) :
    ViewModelBase<HomeData>(
        storage,
        false
    ) {

    override var cacheKey = "home"

    init {
        load(false)
    }

    override suspend fun fetchData(): HomeData {
        return api.getHome()
    }

}
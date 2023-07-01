package com.crstlnz.komikchino.ui.screens.home.fragments.home

import android.util.Log
import com.crstlnz.komikchino.data.api.ScraperBase
import com.crstlnz.komikchino.data.model.HomeData
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ScraperViewModel
import com.crstlnz.komikchino.ui.util.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeFragmenViewModel @Inject constructor(
    @Named("homeFragmentCache") storage: StorageHelper<HomeData>,
    private val api: ScraperBase
) :
    ScraperViewModel<HomeData>(
        storage,
        false
    ) {

    override var cacheKey = "home"

    init {
        Log.d("CACHE", cacheKey)
        load(false)
    }

    override suspend fun fetchData(): HomeData {
        return api.getHome()
    }

}
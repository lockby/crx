package com.crstlnz.komikchino.ui.screens.home.fragments.settings.sub.checkupdate

import com.crstlnz.komikchino.data.api.ApiClient
import com.crstlnz.komikchino.data.model.GithubModel
import com.crstlnz.komikchino.data.util.StorageHelper
import com.crstlnz.komikchino.ui.util.ViewModelBase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CheckUpdateViewModel @Inject constructor(
    @Named("updateCache") storage: StorageHelper<GithubModel>,
) :
    ViewModelBase<GithubModel>(
        storage,
        true
    ) {

    val api = ApiClient.getGithubClient()

    init {
        load(true)
    }

    override suspend fun fetchData(): GithubModel {
        return api.getReleases()
    }

}
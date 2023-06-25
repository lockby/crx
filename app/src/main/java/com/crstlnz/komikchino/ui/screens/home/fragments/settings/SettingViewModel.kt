package com.crstlnz.komikchino.ui.screens.home.fragments.settings

import androidx.lifecycle.ViewModel
import com.crstlnz.komikchino.data.datastore.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    val settings: Settings
) : ViewModel() {

}
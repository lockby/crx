package com.crstlnz.komikchino.data.model

import androidx.compose.runtime.Composable

data class TabRowItem(
    val title: String,
    val screen: @Composable (id: String) -> Unit,
)
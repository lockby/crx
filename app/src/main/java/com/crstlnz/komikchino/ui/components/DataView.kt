package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.DataState
import com.crstlnz.komikchino.ui.util.ViewModelBase
@Composable
fun <T> DataView(
    modifier: Modifier = Modifier,
    viewModel: ViewModelBase<T>,
    onError: (message: String, shouldShowError: Boolean) -> Unit = { _, _ -> },
    loading: (LazyListScope.() -> Unit),
    content: (LazyListScope.(data: T) -> Unit)
) {
    val dataState by viewModel.state.collectAsState()
    when (dataState) {
        is DataState.Success -> {
            LazyColumn(modifier) {
                content((dataState as DataState.Success<T>).data)
            }
        }
        is DataState.Error -> {
            ErrorView(resId = R.drawable.error,
                message = stringResource(R.string.unknown_error),
                buttonName = stringResource(R.string.retry),
                onClick = {
                    viewModel.load()
                })
        }
        else -> LazyColumn(modifier) {
            loading()
        }
    }
}
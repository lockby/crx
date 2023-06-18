package com.crstlnz.komikchino.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.crstlnz.komikchino.R
import com.crstlnz.komikchino.data.model.State
import com.crstlnz.komikchino.ui.util.ViewModelBase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun <T> DataView(
    modifier: Modifier = Modifier,
    viewModel: ViewModelBase<T>,
    checkEmpty: (data: T) -> Boolean = { false },
    onError: (message: String, shouldShowError: Boolean) -> Unit = { _, _ -> },
    loading: (LazyListScope.() -> Unit),
    content: (LazyListScope.(data: T) -> Unit)
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val dataState by viewModel.state.collectAsState()
    val data = dataState.data

    LaunchedEffect(Unit) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            launch {
                viewModel.onError.collectLatest {
                    onError(it, (data == null || checkEmpty(data)))
                }
            }
        }
    }

//    LaunchedEffect(dataState) {
//        scrollBehavior.state.contentOffset = 0F
//    }

    when (dataState.state) {
        State.DATA -> {
            if (data == null || checkEmpty(data)) {
                ErrorView(resId = R.drawable.empty_box,
                    message = stringResource(R.string.empty),
                    onClick = {
                        viewModel.load()
                    })
            } else {
                LazyColumn(modifier) {
                    content(data)
                }
            }
        }

        State.ERROR -> {
            if (data !== null && checkEmpty(data)) {
                LazyColumn(modifier) {
                    content(data)
                }
            } else {
                ErrorView(resId = R.drawable.error,
                    message = stringResource(R.string.unknown_error),
                    buttonName = stringResource(R.string.retry),
                    onClick = {
                        viewModel.load()
                    })
            }
        }

        else -> LazyColumn(modifier) {
            loading()
        }
    }

}
package com.crstlnz.komikchino.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.crstlnz.komikchino.config.AppSettings

//@Composable
//fun CloudflareBlockListener(
//    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
//    onEvent: (Boolean) -> Unit
//) {
//    AppSettings.cloudflareBlock.observe(lifeCycleOwner) {
//        onEvent(it)
//    }
//}
//
//
//@Composable
//fun OnCloudflareUnblock(
//    lifeCycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
//    onEvent: (Boolean) -> Unit
//) {
//    AppSettings.cloudflareBlock.observe(lifeCycleOwner) {
//        if (!it) onEvent(it)
//    }
//}
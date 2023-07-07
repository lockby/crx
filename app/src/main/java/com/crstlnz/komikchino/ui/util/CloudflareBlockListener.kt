package com.crstlnz.komikchino.ui.util

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
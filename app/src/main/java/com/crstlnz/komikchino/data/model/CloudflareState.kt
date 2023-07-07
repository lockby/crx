package com.crstlnz.komikchino.data.model

data class CloudflareState(
    val isBlocked: Boolean,
    val key: Int = 0,
    val isUnblockInProgress: Boolean = false,
    val tryCount: Int = 0,
    val mustManualTrigger: Boolean = false,
    val mustClearCache: Boolean = false,
    val autoReloadConsumed: Boolean = true,
)
package com.crstlnz.komikchino.data.model

data class ScrapeResult<T>(
    val cloudflareBlock: Boolean,
    val result: T
)

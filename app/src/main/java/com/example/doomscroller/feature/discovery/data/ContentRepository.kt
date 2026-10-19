package com.example.doomscroller.feature.discovery.data

import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform

data class SearchResult(
    val items: List<ContentItem>,
    val nextPageToken: String? = null,
    val sourceNotice: String? = null
)

interface ContentRepository {
    suspend fun searchContent(
        topic: String,
        platform: ContentPlatform = ContentPlatform.ALL,
        maxResults: Int = 10,
        pageToken: String? = null
    ): Result<SearchResult>
}

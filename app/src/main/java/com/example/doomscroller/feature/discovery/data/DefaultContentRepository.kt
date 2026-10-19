package com.example.doomscroller.feature.discovery.data

import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DefaultContentRepository(
    private val youTubeRepository: YouTubeRepository,
    private val instagramRepository: InstagramRepository,
    private val discoveryCache: DiscoveryCache
) : ContentRepository {

    override suspend fun searchContent(
        topic: String,
        platform: ContentPlatform,
        maxResults: Int,
        pageToken: String?
    ): Result<SearchResult> {
        val trimmed = topic.trim()
        if (trimmed.isBlank()) {
            return Result.success(SearchResult(emptyList()))
        }

        return when (platform) {
            ContentPlatform.YOUTUBE -> youTubeRepository.searchContent(trimmed, platform, maxResults, pageToken)
            ContentPlatform.INSTAGRAM -> instagramRepository.searchContent(trimmed, platform, maxResults, pageToken)
            ContentPlatform.ALL -> searchAllPlatforms(trimmed, maxResults)
        }
    }

    private suspend fun searchAllPlatforms(topic: String, maxResults: Int): Result<SearchResult> = coroutineScope {
        val ytDeferred = async { youTubeRepository.searchContent(topic, ContentPlatform.YOUTUBE, maxResults / 2 + 1) }
        val igDeferred = async { instagramRepository.searchContent(topic, ContentPlatform.INSTAGRAM, maxResults / 2 + 1) }

        val ytResult = ytDeferred.await().getOrNull()
        val igResult = igDeferred.await().getOrNull()

        val ytItems = ytResult?.items ?: emptyList()
        val igItems = igResult?.items ?: emptyList()

        val merged = mutableListOf<ContentItem>()
        val maxLen = maxOf(ytItems.size, igItems.size)
        for (i in 0 until maxLen) {
            if (i < ytItems.size) merged.add(ytItems[i])
            if (i < igItems.size) merged.add(igItems[i])
        }

        val finalItems = merged.take(maxResults)
        val notice = buildString {
            if (ytResult?.sourceNotice != null) append("YouTube: ${ytResult.sourceNotice}. ")
            if (igResult?.sourceNotice != null) append("Instagram: ${igResult.sourceNotice}")
        }.trim()

        Result.success(
            SearchResult(
                items = finalItems,
                sourceNotice = notice.ifBlank { null }
            )
        )
    }

    fun toggleSave(item: ContentItem): Boolean {
        return discoveryCache.toggleSave(item)
    }

    fun getSavedItems(): List<ContentItem> {
        return discoveryCache.getSavedItems()
    }
}

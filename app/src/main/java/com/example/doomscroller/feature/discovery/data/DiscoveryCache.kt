package com.example.doomscroller.feature.discovery.data

import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory and session cache for content discovery results to minimize external API quota expenditure.
 */
class DiscoveryCache(
    private val cacheTtlMillis: Long = 24 * 60 * 60 * 1000L // 24 hours default
) {
    private data class CacheEntry(
        val timestamp: Long,
        val result: SearchResult
    )

    private val queryCache = ConcurrentHashMap<String, CacheEntry>()
    private val savedItemMap = ConcurrentHashMap<String, ContentItem>()

    fun get(topic: String, platform: ContentPlatform): SearchResult? {
        val key = buildKey(topic, platform)
        val entry = queryCache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > cacheTtlMillis) {
            queryCache.remove(key)
            return null
        }
        return entry.result
    }

    fun put(topic: String, platform: ContentPlatform, result: SearchResult) {
        val key = buildKey(topic, platform)
        queryCache[key] = CacheEntry(
            timestamp = System.currentTimeMillis(),
            result = result
        )
    }

    fun clear() {
        queryCache.clear()
    }

    fun isSaved(itemId: String): Boolean {
        return savedItemMap.containsKey(itemId)
    }

    fun toggleSave(item: ContentItem): Boolean {
        return if (savedItemMap.containsKey(item.id)) {
            savedItemMap.remove(item.id)
            false
        } else {
            savedItemMap[item.id] = item.copy(isSaved = true)
            true
        }
    }

    fun getSavedItems(): List<ContentItem> {
        return savedItemMap.values.toList()
    }

    private fun buildKey(topic: String, platform: ContentPlatform): String {
        return "${platform.name}:${topic.trim().lowercase()}"
    }
}

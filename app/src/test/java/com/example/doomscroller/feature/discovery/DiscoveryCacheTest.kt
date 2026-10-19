package com.example.doomscroller.feature.discovery

import com.example.doomscroller.feature.discovery.data.DiscoveryCache
import com.example.doomscroller.feature.discovery.data.SearchResult
import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscoveryCacheTest {

    private lateinit var cache: DiscoveryCache

    @Before
    fun setUp() {
        cache = DiscoveryCache(cacheTtlMillis = 1000L) // 1 second TTL for test
    }

    @Test
    fun testCachePutAndGet() {
        val item = ContentItem(
            id = "yt_1",
            platform = ContentPlatform.YOUTUBE,
            title = "Algorithmic Complexity",
            creator = "MIT",
            url = "https://youtube.com/watch?v=1"
        )
        val result = SearchResult(items = listOf(item), sourceNotice = "Test Notice")

        cache.put("Algorithms", ContentPlatform.YOUTUBE, result)

        val retrieved = cache.get("algorithms", ContentPlatform.YOUTUBE)
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.items?.size)
        assertEquals("yt_1", retrieved?.items?.first()?.id)
        assertEquals("Test Notice", retrieved?.sourceNotice)
    }

    @Test
    fun testCacheKeyNormalization() {
        val item = ContentItem(
            id = "ig_1",
            platform = ContentPlatform.INSTAGRAM,
            title = "Clean Architecture",
            creator = "Dev",
            url = "https://instagram.com/p/1"
        )
        val result = SearchResult(items = listOf(item))

        cache.put("   Computer Science  ", ContentPlatform.INSTAGRAM, result)

        // Lowercase and trimmed query should hit the same cache entry
        val retrieved = cache.get("computer science", ContentPlatform.INSTAGRAM)
        assertNotNull(retrieved)
        assertEquals("ig_1", retrieved?.items?.first()?.id)

        // Different platform should not hit cache
        val ytRetrieved = cache.get("computer science", ContentPlatform.YOUTUBE)
        assertNull(ytRetrieved)
    }

    @Test
    fun testCacheTtlExpiration() {
        val item = ContentItem(
            id = "yt_exp",
            platform = ContentPlatform.YOUTUBE,
            title = "Expiring Item",
            creator = "Creator",
            url = "https://youtube.com"
        )
        val result = SearchResult(items = listOf(item))

        // Create cache with 1ms TTL
        val shortCache = DiscoveryCache(cacheTtlMillis = 1L)
        shortCache.put("fast", ContentPlatform.YOUTUBE, result)

        Thread.sleep(10)

        val retrieved = shortCache.get("fast", ContentPlatform.YOUTUBE)
        assertNull("Cache entry should expire after TTL", retrieved)
    }

    @Test
    fun testToggleSaveAndGetSavedItems() {
        val item1 = ContentItem(
            id = "item_1",
            platform = ContentPlatform.YOUTUBE,
            title = "Item 1",
            creator = "Creator 1",
            url = "https://youtube.com/1"
        )
        val item2 = ContentItem(
            id = "item_2",
            platform = ContentPlatform.INSTAGRAM,
            title = "Item 2",
            creator = "Creator 2",
            url = "https://instagram.com/2"
        )

        assertFalse(cache.isSaved("item_1"))

        // Save item 1
        val isSaved1 = cache.toggleSave(item1)
        assertTrue(isSaved1)
        assertTrue(cache.isSaved("item_1"))
        assertEquals(1, cache.getSavedItems().size)

        // Save item 2
        val isSaved2 = cache.toggleSave(item2)
        assertTrue(isSaved2)
        assertEquals(2, cache.getSavedItems().size)

        // Unsave item 1
        val isSavedAgain = cache.toggleSave(item1)
        assertFalse(isSavedAgain)
        assertFalse(cache.isSaved("item_1"))
        assertEquals(1, cache.getSavedItems().size)
        assertEquals("item_2", cache.getSavedItems().first().id)
    }
}

package com.example.doomscroller.feature.discovery

import com.example.doomscroller.feature.discovery.data.DefaultContentRepository
import com.example.doomscroller.feature.discovery.data.DiscoveryCache
import com.example.doomscroller.feature.discovery.data.InstagramRepository
import com.example.doomscroller.feature.discovery.data.YouTubeRepository
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscoveryRepositoriesTest {

    private lateinit var cache: DiscoveryCache
    private lateinit var youTubeRepository: YouTubeRepository
    private lateinit var instagramRepository: InstagramRepository
    private lateinit var defaultContentRepository: DefaultContentRepository

    @Before
    fun setUp() {
        cache = DiscoveryCache()
        youTubeRepository = YouTubeRepository(discoveryCache = cache)
        instagramRepository = InstagramRepository(discoveryCache = cache)
        defaultContentRepository = DefaultContentRepository(
            youTubeRepository = youTubeRepository,
            instagramRepository = instagramRepository,
            discoveryCache = cache
        )
    }

    @Test
    fun testYouTubeRepositoryCuratedFallbackReturnsContent() = runBlocking {
        val result = youTubeRepository.searchContent("Computer Science", ContentPlatform.YOUTUBE, maxResults = 5)
        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()
        assertNotNull(searchResult)
        assertFalse(searchResult!!.items.isEmpty())
        assertTrue(searchResult.items.size <= 5)
        assertTrue(searchResult.items.all { it.platform == ContentPlatform.YOUTUBE })
        assertNotNull(searchResult.sourceNotice)
    }

    @Test
    fun testYouTubeRepositoryBlankQueryReturnsEmptyList() = runBlocking {
        val result = youTubeRepository.searchContent("   ", ContentPlatform.YOUTUBE)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.items.isEmpty())
    }

    @Test
    fun testInstagramRepositoryCuratedContentAndDirectTagLink() = runBlocking {
        val result = instagramRepository.searchContent("Algorithms", ContentPlatform.INSTAGRAM, maxResults = 5)
        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()
        assertNotNull(searchResult)
        assertFalse(searchResult!!.items.isEmpty())
        assertTrue(searchResult.items.all { it.platform == ContentPlatform.INSTAGRAM })
        
        // Check direct topic stream item exists and has tag URL
        val directTagItem = searchResult.items.find { it.id.startsWith("ig_topic_direct") }
        assertNotNull(directTagItem)
        assertTrue(directTagItem!!.url.contains("explore/tags/"))
        assertNotNull(searchResult.sourceNotice)
    }

    @Test
    fun testDefaultContentRepositoryPlatformAllMergesBothPlatforms() = runBlocking {
        val result = defaultContentRepository.searchContent("Computer Science", ContentPlatform.ALL, maxResults = 6)
        assertTrue(result.isSuccess)
        val items = result.getOrNull()!!.items
        assertFalse(items.isEmpty())
        assertTrue(items.size <= 6)

        val hasYouTube = items.any { it.platform == ContentPlatform.YOUTUBE }
        val hasInstagram = items.any { it.platform == ContentPlatform.INSTAGRAM }
        assertTrue("Merged results should include YouTube items", hasYouTube)
        assertTrue("Merged results should include Instagram items", hasInstagram)
    }

    @Test
    fun testDefaultContentRepositorySaveTogglePreservesState() = runBlocking {
        val result = defaultContentRepository.searchContent("System Design", ContentPlatform.YOUTUBE, maxResults = 1)
        val item = result.getOrNull()!!.items.first()

        assertFalse(item.isSaved)
        val saved = defaultContentRepository.toggleSave(item)
        assertTrue(saved)

        val savedList = defaultContentRepository.getSavedItems()
        assertEquals(1, savedList.size)
        assertEquals(item.id, savedList.first().id)
        assertTrue(savedList.first().isSaved)
    }
}

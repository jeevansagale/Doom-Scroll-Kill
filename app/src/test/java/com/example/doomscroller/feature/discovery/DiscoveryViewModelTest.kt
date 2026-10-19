package com.example.doomscroller.feature.discovery

import com.example.doomscroller.feature.discovery.data.DefaultContentRepository
import com.example.doomscroller.feature.discovery.data.DiscoveryCache
import com.example.doomscroller.feature.discovery.data.InstagramRepository
import com.example.doomscroller.feature.discovery.data.YouTubeRepository
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import com.example.doomscroller.feature.discovery.ui.DiscoveryUiState
import com.example.doomscroller.feature.discovery.ui.DiscoveryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiscoveryViewModelTest {

    private lateinit var cache: DiscoveryCache
    private lateinit var repository: DefaultContentRepository
    private val testScope = CoroutineScope(Dispatchers.Unconfined)

    @Before
    fun setUp() {
        cache = DiscoveryCache()
        val yt = YouTubeRepository(discoveryCache = cache)
        val ig = InstagramRepository(discoveryCache = cache)
        repository = DefaultContentRepository(yt, ig, cache)
    }

    private fun waitForResults(viewModel: DiscoveryViewModel, timeoutMs: Long = 2000L): DiscoveryUiState {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val state = viewModel.uiState.value
            if (!state.isLoading && state.results.isNotEmpty()) {
                return state
            }
            Thread.sleep(20)
        }
        return viewModel.uiState.value
    }

    @Test
    fun testInitialStateAndSearchExecution() {
        val viewModel = DiscoveryViewModel(repository, testScope)
        val state = waitForResults(viewModel)

        assertEquals("Computer Science", state.query)
        assertEquals(ContentPlatform.ALL, state.selectedPlatform)
        assertFalse(state.isLoading)
        assertFalse(state.results.isEmpty())
        assertEquals(0, state.activeTab)
    }

    @Test
    fun testTopicPillClickUpdatesQueryAndResults() {
        val viewModel = DiscoveryViewModel(repository, testScope)
        waitForResults(viewModel)

        viewModel.onTopicPillClick("System Design")
        val state = waitForResults(viewModel)

        assertEquals("System Design", state.query)
        assertFalse(state.results.isEmpty())
        assertTrue(state.results.any { it.title.contains("System Design", ignoreCase = true) || it.topic.contains("System Design", ignoreCase = true) })
    }

    @Test
    fun testPlatformFilterSelection() {
        val viewModel = DiscoveryViewModel(repository, testScope)
        waitForResults(viewModel)

        viewModel.onPlatformSelect(ContentPlatform.YOUTUBE)
        val ytState = waitForResults(viewModel)
        assertEquals(ContentPlatform.YOUTUBE, ytState.selectedPlatform)
        assertTrue(ytState.results.all { it.platform == ContentPlatform.YOUTUBE })

        viewModel.onPlatformSelect(ContentPlatform.INSTAGRAM)
        val igState = waitForResults(viewModel)
        assertEquals(ContentPlatform.INSTAGRAM, igState.selectedPlatform)
        assertTrue(igState.results.all { it.platform == ContentPlatform.INSTAGRAM })
    }

    @Test
    fun testIntentionModalWorkflow() {
        val viewModel = DiscoveryViewModel(repository, testScope)
        val state = waitForResults(viewModel)

        val firstItem = state.results.first()
        viewModel.onOpenItemClick(firstItem)

        assertEquals(firstItem.id, viewModel.uiState.value.selectedItemForIntention?.id)

        viewModel.onDismissIntention()
        assertNull(viewModel.uiState.value.selectedItemForIntention)
    }

    @Test
    fun testSaveItemAndTabSwitching() {
        val viewModel = DiscoveryViewModel(repository, testScope)
        val state = waitForResults(viewModel)

        val itemToSave = state.results.first()
        viewModel.onToggleSave(itemToSave)

        assertEquals(1, viewModel.uiState.value.savedItems.size)
        assertEquals(itemToSave.id, viewModel.uiState.value.savedItems.first().id)

        // Switch to Saved tab
        viewModel.onTabSelect(1)
        assertEquals(1, viewModel.uiState.value.activeTab)
        assertEquals(1, viewModel.uiState.value.savedItems.size)

        // Unsave item
        viewModel.onToggleSave(itemToSave)
        assertTrue(viewModel.uiState.value.savedItems.isEmpty())
    }
}

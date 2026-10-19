package com.example.doomscroller.feature.discovery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doomscroller.feature.discovery.data.DefaultContentRepository
import com.example.doomscroller.feature.discovery.data.DiscoveryCache
import com.example.doomscroller.feature.discovery.data.InstagramRepository
import com.example.doomscroller.feature.discovery.data.YouTubeRepository
import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoveryUiState(
    val query: String = "Computer Science",
    val selectedPlatform: ContentPlatform = ContentPlatform.ALL,
    val isLoading: Boolean = false,
    val results: List<ContentItem> = emptyList(),
    val savedItems: List<ContentItem> = emptyList(),
    val activeTab: Int = 0, // 0 = Discover, 1 = Saved
    val sourceNotice: String? = null,
    val selectedItemForIntention: ContentItem? = null,
    val errorMessage: String? = null,
    val suggestedTopics: List<String> = listOf(
        "Computer Science",
        "System Design",
        "DSA & Algorithms",
        "Android Development",
        "AI & Mathematics",
        "Mindfulness & Focus",
        "Science & Physics"
    )
)

class DiscoveryViewModel(
    private val repository: DefaultContentRepository = createDefaultRepository(),
    private val scopeOverride: kotlinx.coroutines.CoroutineScope? = null
) : ViewModel() {

    private val currentScope: kotlinx.coroutines.CoroutineScope
        get() = scopeOverride ?: viewModelScope

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Initial search on default topic
        performSearch(_uiState.value.query, _uiState.value.selectedPlatform)
        refreshSavedItems()
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = currentScope.launch {
            delay(500) // 500ms debounce
            performSearch(newQuery, _uiState.value.selectedPlatform)
        }
    }

    fun onPlatformSelect(platform: ContentPlatform) {
        if (_uiState.value.selectedPlatform == platform) return
        _uiState.update { it.copy(selectedPlatform = platform) }
        performSearch(_uiState.value.query, platform)
    }

    fun onTopicPillClick(topic: String) {
        _uiState.update { it.copy(query = topic) }
        performSearch(topic, _uiState.value.selectedPlatform)
    }

    fun onTabSelect(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
        if (tabIndex == 1) {
            refreshSavedItems()
        }
    }

    fun onOpenItemClick(item: ContentItem) {
        _uiState.update { it.copy(selectedItemForIntention = item) }
    }

    fun onDismissIntention() {
        _uiState.update { it.copy(selectedItemForIntention = null) }
    }

    fun onToggleSave(item: ContentItem) {
        repository.toggleSave(item)
        refreshSavedItems()
        // Update item in results
        _uiState.update { state ->
            val updatedResults = state.results.map {
                if (it.id == item.id) it.copy(isSaved = !it.isSaved) else it
            }
            state.copy(results = updatedResults)
        }
    }

    private fun refreshSavedItems() {
        val saved = repository.getSavedItems()
        _uiState.update { it.copy(savedItems = saved) }
    }

    fun performSearch(query: String, platform: ContentPlatform) {
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isLoading = false,
                    sourceNotice = null,
                    errorMessage = null
                )
            }
            return
        }

        currentScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.searchContent(query, platform, maxResults = 10)
            result.onSuccess { searchResult ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        results = searchResult.items,
                        sourceNotice = searchResult.sourceNotice,
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load content"
                    )
                }
            }
        }
    }

    companion object {
        fun createDefaultRepository(): DefaultContentRepository {
            val cache = DiscoveryCache()
            val yt = YouTubeRepository(discoveryCache = cache)
            val ig = InstagramRepository(discoveryCache = cache)
            return DefaultContentRepository(yt, ig, cache)
        }
    }
}

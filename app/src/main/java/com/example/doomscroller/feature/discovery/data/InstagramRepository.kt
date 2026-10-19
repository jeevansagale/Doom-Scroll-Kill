package com.example.doomscroller.feature.discovery.data

import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstagramRepository(
    private val discoveryCache: DiscoveryCache? = null
) : ContentRepository {

    override suspend fun searchContent(
        topic: String,
        platform: ContentPlatform,
        maxResults: Int,
        pageToken: String?
    ): Result<SearchResult> = withContext(Dispatchers.IO) {
        val trimmedTopic = topic.trim()
        if (trimmedTopic.isBlank()) {
            return@withContext Result.success(SearchResult(items = emptyList()))
        }

        // Check cache if no pagination token
        if (pageToken == null && discoveryCache != null) {
            discoveryCache.get(trimmedTopic, ContentPlatform.INSTAGRAM)?.let { cached ->
                return@withContext Result.success(cached)
            }
        }

        val items = getCuratedInstagramContent(trimmedTopic, maxResults)
        val result = SearchResult(
            items = items,
            sourceNotice = "Curated educational creators & intentional direct-topic links (Meta official API does not support public keyword search for consumers)"
        )

        if (pageToken == null) {
            discoveryCache?.put(trimmedTopic, ContentPlatform.INSTAGRAM, result)
        }

        Result.success(result)
    }

    private fun getCuratedInstagramContent(topic: String, limit: Int): List<ContentItem> {
        val cleanTag = topic.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
        val tagSearchUrl = if (cleanTag.isNotBlank()) {
            "https://www.instagram.com/explore/tags/$cleanTag/"
        } else {
            "https://www.instagram.com/explore/tags/programming/"
        }

        val curatedCatalog = listOf(
            ContentItem(
                id = "ig_topic_direct_$cleanTag",
                platform = ContentPlatform.INSTAGRAM,
                title = "Intentional Topic Stream: #${cleanTag.ifBlank { "education" }}",
                creator = "Intentional Tag Discovery",
                url = tagSearchUrl,
                durationText = "Direct Tag",
                topic = topic,
                description = "Open Instagram directly into the curated #${cleanTag.ifBlank { "education" }} tag feed, bypassing algorithmic home suggestions."
            ),
            ContentItem(
                id = "ig_thecodersblog",
                platform = ContentPlatform.INSTAGRAM,
                title = "Clean Architecture & Design Patterns",
                creator = "@thecodersblog",
                url = "https://www.instagram.com/thecodersblog/",
                durationText = "Creator Profile",
                topic = "Computer Science",
                description = "Concise visual breakdowns of SOLID principles, hexagonal architecture, and design patterns."
            ),
            ContentItem(
                id = "ig_developerspace",
                platform = ContentPlatform.INSTAGRAM,
                title = "System Design & Distributed Data",
                creator = "@developerspace",
                url = "https://www.instagram.com/developerspace/",
                durationText = "Creator Profile",
                topic = "System Design",
                description = "Architecture infographics explaining message queues, sharding, and consensus protocols."
            ),
            ContentItem(
                id = "ig_daily_math",
                platform = ContentPlatform.INSTAGRAM,
                title = "Visual Geometry & Number Theory Insights",
                creator = "@brilliantorg",
                url = "https://www.instagram.com/brilliantorg/",
                durationText = "Creator Profile",
                topic = "Science & Mathematics",
                description = "Interactive conceptual math puzzles and geometric intuition."
            ),
            ContentItem(
                id = "ig_mindful_habit",
                platform = ContentPlatform.INSTAGRAM,
                title = "Digital Minimalism & Habit Science",
                creator = "@hubermanlab",
                url = "https://www.instagram.com/hubermanlab/",
                durationText = "Creator Profile",
                topic = "Mindfulness & Focus",
                description = "Evidence-based protocols to reclaim focus and eliminate compulsive digital scrolling."
            ),
            ContentItem(
                id = "ig_android_weekly",
                platform = ContentPlatform.INSTAGRAM,
                title = "Android & Kotlin Dev Tips",
                creator = "@androiddevelopers",
                url = "https://www.instagram.com/androiddevelopers/",
                durationText = "Official Account",
                topic = "Android Development",
                description = "Official Android updates, Compose performance tips, and architectural recommendations."
            ),
            ContentItem(
                id = "ig_ai_research",
                platform = ContentPlatform.INSTAGRAM,
                title = "Modern Machine Learning Insights",
                creator = "@paperswithcode",
                url = "https://www.instagram.com/paperswithcode/",
                durationText = "Creator Profile",
                topic = "AI & Mathematics",
                description = "Visual summaries of top AI research papers, attention mechanisms, and benchmarks."
            )
        )

        val filtered = curatedCatalog.filter {
            it.id.startsWith("ig_topic_direct") ||
                    it.topic.contains(topic, ignoreCase = true) ||
                    it.title.contains(topic, ignoreCase = true) ||
                    it.description.contains(topic, ignoreCase = true)
        }

        val resultList = if (filtered.isNotEmpty()) filtered else curatedCatalog
        return resultList.take(limit).map { item ->
            item.copy(isSaved = discoveryCache?.isSaved(item.id) == true)
        }
    }
}

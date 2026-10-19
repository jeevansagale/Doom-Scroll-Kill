package com.example.doomscroller.feature.discovery.data

import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeRepository(
    private var apiKey: String = "",
    private val discoveryCache: DiscoveryCache? = null
) : ContentRepository {

    fun setApiKey(key: String) {
        this.apiKey = key.trim()
    }

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
            discoveryCache.get(trimmedTopic, ContentPlatform.YOUTUBE)?.let { cached ->
                return@withContext Result.success(cached)
            }
        }

        if (apiKey.isBlank()) {
            val curated = getCuratedYouTubeContent(trimmedTopic, maxResults)
            val result = SearchResult(
                items = curated,
                sourceNotice = "Showing verified educational YouTube content (Add YouTube API key for live search)"
            )
            discoveryCache?.put(trimmedTopic, ContentPlatform.YOUTUBE, result)
            return@withContext Result.success(result)
        }

        try {
            val encodedQuery = URLEncoder.encode(trimmedTopic, "UTF-8")
            val urlBuilder = StringBuilder(
                "https://www.googleapis.com/youtube/v3/search" +
                        "?part=snippet&type=video&safeSearch=strict&relevanceLanguage=en" +
                        "&maxResults=$maxResults&q=$encodedQuery&key=$apiKey"
            )
            if (!pageToken.isNullOrBlank()) {
                urlBuilder.append("&pageToken=").append(URLEncoder.encode(pageToken, "UTF-8"))
            }

            val connection = (URL(urlBuilder.toString()).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseText = reader.use { it.readText() }
                connection.disconnect()

                val json = JSONObject(responseText)
                val itemsArray = json.optJSONArray("items")
                val nextPageToken = if (json.has("nextPageToken")) json.optString("nextPageToken") else null
                val mappedList = mutableListOf<ContentItem>()

                if (itemsArray != null) {
                    for (i in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(i)
                        val idObj = itemObj.optJSONObject("id")
                        val videoId = idObj?.optString("videoId") ?: continue
                        val snippet = itemObj.optJSONObject("snippet") ?: continue

                        val rawTitle = snippet.optString("title", "Educational Video")
                        val channelTitle = snippet.optString("channelTitle", "YouTube Creator")
                        val description = snippet.optString("description", "")
                        val thumbnails = snippet.optJSONObject("thumbnails")
                        val mediumThumb = thumbnails?.optJSONObject("medium")?.optString("url")
                            ?: thumbnails?.optJSONObject("default")?.optString("url")
                            ?: ""

                        mappedList.add(
                            ContentItem(
                                id = "yt_$videoId",
                                platform = ContentPlatform.YOUTUBE,
                                title = decodeHtmlEntities(rawTitle),
                                creator = channelTitle,
                                url = "https://www.youtube.com/watch?v=$videoId",
                                thumbnailUrl = mediumThumb,
                                durationText = "Video",
                                topic = trimmedTopic,
                                description = decodeHtmlEntities(description),
                                isSaved = discoveryCache?.isSaved("yt_$videoId") == true
                            )
                        )
                    }
                }

                val searchResult = SearchResult(
                    items = mappedList,
                    nextPageToken = nextPageToken,
                    sourceNotice = "Live YouTube Data API results"
                )
                if (pageToken == null) {
                    discoveryCache?.put(trimmedTopic, ContentPlatform.YOUTUBE, searchResult)
                }
                Result.success(searchResult)
            } else {
                connection.disconnect()
                // Graceful fallback on quota exhaustion (403) or other HTTP error
                val curated = getCuratedYouTubeContent(trimmedTopic, maxResults)
                val fallbackResult = SearchResult(
                    items = curated,
                    sourceNotice = "YouTube quota limit reached or API error (HTTP $responseCode). Displaying verified curated learning content."
                )
                Result.success(fallbackResult)
            }
        } catch (e: Exception) {
            val curated = getCuratedYouTubeContent(trimmedTopic, maxResults)
            Result.success(
                SearchResult(
                    items = curated,
                    sourceNotice = "Offline mode. Displaying verified curated learning content."
                )
            )
        }
    }

    private fun decodeHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun getCuratedYouTubeContent(topic: String, limit: Int): List<ContentItem> {
        val curatedCatalog = listOf(
            ContentItem(
                id = "yt_mit_algorithms",
                platform = ContentPlatform.YOUTUBE,
                title = "Introduction to Algorithms - Lecture 1",
                creator = "MIT OpenCourseWare",
                url = "https://www.youtube.com/watch?v=ZA-tUyM_y7s",
                durationText = "Lecture",
                topic = "Computer Science",
                description = "Fundamental algorithmic thinking, asymptotic analysis, and problem decomposition."
            ),
            ContentItem(
                id = "yt_system_design",
                platform = ContentPlatform.YOUTUBE,
                title = "System Design Primer: Scalability & Caching",
                creator = "Gaurav Sen",
                url = "https://www.youtube.com/watch?v=xpDnVSmNFX0",
                durationText = "18 min",
                topic = "System Design",
                description = "Deep dive into distributed cache invalidation, write-through vs write-back architectures."
            ),
            ContentItem(
                id = "yt_3blue1brown_nn",
                platform = ContentPlatform.YOUTUBE,
                title = "Neural Networks: But what is a neural network?",
                creator = "3Blue1Brown",
                url = "https://www.youtube.com/watch?v=aircAruvnKk",
                durationText = "19 min",
                topic = "AI & Mathematics",
                description = "Visual, mathematical intuition for deep learning, linear layers, and activation weights."
            ),
            ContentItem(
                id = "yt_huberman_focus",
                platform = ContentPlatform.YOUTUBE,
                title = "Focus and Attention Protocols for Deep Work",
                creator = "Andrew Huberman",
                url = "https://www.youtube.com/watch?v=a9y_0W9eN_U",
                durationText = "22 min",
                topic = "Mindfulness & Focus",
                description = "Neurobiology of dopamine, ultradian cycles, and preventing distraction loops."
            ),
            ContentItem(
                id = "yt_veritasium_quantum",
                platform = ContentPlatform.YOUTUBE,
                title = "How Quantum Computers Work",
                creator = "Veritasium",
                url = "https://www.youtube.com/watch?v=JhHMJCUmq28",
                durationText = "15 min",
                topic = "Science & Physics",
                description = "Superposition, entanglement, and computational complexity visualized."
            ),
            ContentItem(
                id = "yt_android_compose",
                platform = ContentPlatform.YOUTUBE,
                title = "Modern Android: Advanced Jetpack Compose Architecture",
                creator = "Android Developers",
                url = "https://www.youtube.com/watch?v=p1h4C2-Hj8g",
                durationText = "30 min",
                topic = "Android Development",
                description = "State hoisting, stability, recomposition optimization, and adaptive layouts."
            ),
            ContentItem(
                id = "yt_dsa_trees",
                platform = ContentPlatform.YOUTUBE,
                title = "Binary Trees and Graph Traversal Algorithms",
                creator = "freeCodeCamp",
                url = "https://www.youtube.com/watch?v=tWVWeAqZ0WU",
                durationText = "45 min",
                topic = "DSA & Algorithms",
                description = "Breadth-first and depth-first traversal patterns with asymptotic proofs."
            )
        )

        val filtered = curatedCatalog.filter {
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

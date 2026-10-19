package com.example.doomscroller.feature.discovery.domain

/**
 * Common content item model representing an intentional learning resource
 * from YouTube or Instagram.
 */
data class ContentItem(
    val id: String,
    val platform: ContentPlatform,
    val title: String,
    val creator: String,
    val url: String,
    val thumbnailUrl: String = "",
    val durationText: String? = null,
    val topic: String = "",
    val description: String = "",
    val isSaved: Boolean = false
)

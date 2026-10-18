package com.example.doomscroller.domain.quotes

enum class QuoteCategory(val displayName: String) {
    DISCIPLINE("Discipline"),
    CONSISTENCY("Consistency"),
    TIME("Value of Time"),
    LEARNING("Active Learning"),
    SELF_CONTROL("Self-Control"),
    RECOVERY("Starting Again")
}

data class MotivationalQuote(
    val id: String,
    val text: String,
    val category: QuoteCategory,
    val targetContext: String = "general", // "instagram", "youtube", "snapchat", "gaming", "general"
    val author: String = "DOOMSCROLL PHILOSOPHY"
)

object MotivationalQuoteEngine {

    val allQuotes: List<MotivationalQuote> = listOf(
        // --- Instagram Context (Comparison / Infinite Feed) ---
        MotivationalQuote(
            id = "quote_ig_01",
            text = "Do not trade your real life for curated highlights of someone else's.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "instagram"
        ),
        MotivationalQuote(
            id = "quote_ig_02",
            text = "The scroll is endless, but your day is not. Look up and build something tangible.",
            category = QuoteCategory.TIME,
            targetContext = "instagram"
        ),
        MotivationalQuote(
            id = "quote_ig_03",
            text = "Comparison drains the energy you need to improve your own craft.",
            category = QuoteCategory.DISCIPLINE,
            targetContext = "instagram"
        ),
        MotivationalQuote(
            id = "quote_ig_04",
            text = "Every minute spent watching strangers live is a minute not spent living yourself.",
            category = QuoteCategory.TIME,
            targetContext = "instagram"
        ),

        // --- YouTube Context (Passive Viewing vs Active Doing) ---
        MotivationalQuote(
            id = "quote_yt_01",
            text = "Consuming tutorials does not equal building skills. Close the player and write the code.",
            category = QuoteCategory.LEARNING,
            targetContext = "youtube"
        ),
        MotivationalQuote(
            id = "quote_yt_02",
            text = "Autoplay is designed for the platform's revenue, not your personal growth.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "youtube"
        ),
        MotivationalQuote(
            id = "quote_yt_03",
            text = "Spectating is comfortable; creating requires courage. Choose creation today.",
            category = QuoteCategory.DISCIPLINE,
            targetContext = "youtube"
        ),
        MotivationalQuote(
            id = "quote_yt_04",
            text = "Ten minutes of deliberate practice beats two hours of background playback.",
            category = QuoteCategory.CONSISTENCY,
            targetContext = "youtube"
        ),

        // --- Snapchat Context (Streak Anxiety / Superficial Urgency) ---
        MotivationalQuote(
            id = "quote_sc_01",
            text = "Streaks in an app mean nothing if your daily discipline streak is broken.",
            category = QuoteCategory.CONSISTENCY,
            targetContext = "snapchat"
        ),
        MotivationalQuote(
            id = "quote_sc_02",
            text = "Manufactured urgency is not true connection. Invest your presence where it counts.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "snapchat"
        ),
        MotivationalQuote(
            id = "quote_sc_03",
            text = "Protect your attention: it is the rarest and most valuable currency you possess.",
            category = QuoteCategory.TIME,
            targetContext = "snapchat"
        ),

        // --- Games Context (Virtual Trophies vs Real Competence) ---
        MotivationalQuote(
            id = "quote_game_01",
            text = "Leveling up a virtual character will never substitute for leveling up your real life.",
            category = QuoteCategory.DISCIPLINE,
            targetContext = "gaming"
        ),
        MotivationalQuote(
            id = "quote_game_02",
            text = "Game mechanics are engineered for dopamine loops. Real mastery requires real friction.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "gaming"
        ),
        MotivationalQuote(
            id = "quote_game_03",
            text = "Win the match that actually matters: mastering your daily habits.",
            category = QuoteCategory.CONSISTENCY,
            targetContext = "gaming"
        ),

        // --- Discipline & Consistency (General) ---
        MotivationalQuote(
            id = "quote_gen_01",
            text = "Discipline is choosing between what you want now and what you want most.",
            category = QuoteCategory.DISCIPLINE,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_02",
            text = "Small, consistent efforts compound quietly into undeniable results.",
            category = QuoteCategory.CONSISTENCY,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_03",
            text = "Motivation gets you started; disciplined systems keep you moving forward.",
            category = QuoteCategory.DISCIPLINE,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_04",
            text = "You do not need ideal conditions to make progress; you only need to begin.",
            category = QuoteCategory.CONSISTENCY,
            targetContext = "general"
        ),

        // --- Time & Self-Control (General) ---
        MotivationalQuote(
            id = "quote_gen_05",
            text = "Lost money can be earned again. Lost time is gone forever.",
            category = QuoteCategory.TIME,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_06",
            text = "Master your impulses before they dictate the direction of your life.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_07",
            text = "Silence the noise so you can hear the thoughts that actually matter.",
            category = QuoteCategory.SELF_CONTROL,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_08",
            text = "Every interruption carries a cognitive tax. Guard your focus fiercely.",
            category = QuoteCategory.TIME,
            targetContext = "general"
        ),

        // --- Learning & Recovery (General) ---
        MotivationalQuote(
            id = "quote_gen_09",
            text = "A slip in discipline does not erase your trajectory unless you surrender to it.",
            category = QuoteCategory.RECOVERY,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_10",
            text = "The stumble happened. Stand back up right now; do not wait until tomorrow.",
            category = QuoteCategory.RECOVERY,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_11",
            text = "Curiosity directed toward deep knowledge turns wasted hours into lifelong mastery.",
            category = QuoteCategory.LEARNING,
            targetContext = "general"
        ),
        MotivationalQuote(
            id = "quote_gen_12",
            text = "Difficult concepts yield to sustained attention. Stay with the problem a little longer.",
            category = QuoteCategory.LEARNING,
            targetContext = "general"
        )
    )

    fun resolveTargetContext(packageName: String): String {
        val lower = packageName.lowercase()
        return when {
            lower.contains("instagram") -> "instagram"
            lower.contains("youtube") -> "youtube"
            lower.contains("snapchat") -> "snapchat"
            lower.contains("game") || lower.contains("supercell") || lower.contains("roblox") || lower.contains("riot") -> "gaming"
            else -> "general"
        }
    }

    fun getQuoteForPackage(packageName: String, excludeId: String? = null): MotivationalQuote {
        val context = resolveTargetContext(packageName)
        val matchingContext = allQuotes.filter { it.targetContext == context && it.id != excludeId }
        val pool = if (matchingContext.isNotEmpty()) {
            matchingContext
        } else {
            allQuotes.filter { it.id != excludeId }.ifEmpty { allQuotes }
        }
        return pool.random()
    }

    fun getQuoteById(id: String?): MotivationalQuote? {
        if (id.isNullOrBlank()) return null
        return allQuotes.find { it.id == id }
    }
}

package com.example.doomscroller.domain.quotes

enum class QuoteCategory(val displayName: String) {
    ATTENTION_FOCUS("Attention & Focus"),
    TIME_PRIORITIES("Time & Priorities"),
    MINDFULNESS_INTENTION("Mindfulness & Intentional Behavior"),
    DISCIPLINE_HABITS("Discipline & Habit Formation"),
    REST_PRESENCE("Rest & Being Present"),
    DIGITAL_BALANCE("Digital Balance & Personal Agency"),
    // Legacy categories preserved for backwards compatibility
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
    val author: String = "DOOMSCROLL PHILOSOPHY",
    val attributionUrl: String? = null
) {
    init {
        require(id.isNotBlank()) { "Quote ID must not be blank" }
        require(text.isNotBlank()) { "Quote text must not be blank for $id" }
        require(author.isNotBlank()) { "Quote author must not be blank for $id" }
    }
}

object MotivationalQuoteEngine {

    val allQuotes: List<MotivationalQuote> = listOf(
        // =========================================================================
        // 1. Attention & Focus
        // =========================================================================
        MotivationalQuote(
            id = "quote_att_01",
            text = "Your attention is your life. What you pay attention to becomes your biography.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Mary Oliver",
            attributionUrl = "https://www.poetryfoundation.org/poets/mary-oliver"
        ),
        MotivationalQuote(
            id = "quote_att_02",
            text = "Clarity about what matters provides clarity about what does not.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Cal Newport",
            attributionUrl = "https://calnewport.com/books/deep-work/"
        ),
        MotivationalQuote(
            id = "quote_att_03",
            text = "The ability to perform deep work is becoming increasingly rare at exactly the same time it is becoming increasingly valuable.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Cal Newport",
            attributionUrl = "https://calnewport.com/books/deep-work/"
        ),
        MotivationalQuote(
            id = "quote_att_04",
            text = "What we choose to focus on and what we choose to ignore—plays in defining the quality of our life.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Mihaly Csikszentmihalyi",
            attributionUrl = "https://www.goodreads.com/book/show/66354.Flow"
        ),
        MotivationalQuote(
            id = "quote_att_05",
            text = "If you want to master focus, you must become ruthless about what you allow into your awareness.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_att_06",
            text = "A fragmented mind cannot build profound things. Protect your depth.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_att_07",
            text = "To be everywhere is to be nowhere.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Seneca",
            attributionUrl = "https://en.wikisource.org/wiki/Moral_letters_to_Lucilius/Letter_2"
        ),
        MotivationalQuote(
            id = "quote_att_08",
            text = "Deep focus is not an accident; it is the deliberate refusal of easy distractions.",
            category = QuoteCategory.ATTENTION_FOCUS,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),

        // =========================================================================
        // 2. Time & Priorities
        // =========================================================================
        MotivationalQuote(
            id = "quote_time_01",
            text = "How we spend our days is, of course, how we spend our lives.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Annie Dillard",
            attributionUrl = "https://www.goodreads.com/work/quotes/1169603"
        ),
        MotivationalQuote(
            id = "quote_time_02",
            text = "It is not that we have a short time to live, but that we waste a lot of it.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Seneca",
            attributionUrl = "https://en.wikisource.org/wiki/On_the_Shortness_of_Life"
        ),
        MotivationalQuote(
            id = "quote_time_03",
            text = "The cost of a thing is the amount of what I will call life which is required to be exchanged for it.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Henry David Thoreau",
            attributionUrl = "https://en.wikisource.org/wiki/Walden/Economy"
        ),
        MotivationalQuote(
            id = "quote_time_04",
            text = "The average human lifespan is roughly four thousand weeks. Choose where this hour goes.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Oliver Burkeman",
            attributionUrl = "https://www.oliverburkeman.com/fourthousandweeks"
        ),
        MotivationalQuote(
            id = "quote_time_05",
            text = "Infinite feeds will eagerly consume every hour you surrender. Allocate your time deliberately.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_time_06",
            text = "You will never find time for anything worthwhile. If you want time you must make it.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Charles Buxton",
            attributionUrl = "https://www.goodreads.com/author/quotes/432092.Charles_Buxton"
        ),
        MotivationalQuote(
            id = "quote_time_07",
            text = "Lost money can be earned again. Lost time is vanished permanently.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_time_08",
            text = "Every notification you heed is a silent contract exchanging your minutes for someone else's metrics.",
            category = QuoteCategory.TIME_PRIORITIES,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),

        // =========================================================================
        // 3. Mindfulness & Intentional Behavior
        // =========================================================================
        MotivationalQuote(
            id = "quote_mind_01",
            text = "Between stimulus and response there is a space. In that space is our power to choose our response.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Viktor E. Frankl",
            attributionUrl = "https://www.goodreads.com/work/quotes/20689-man-s-search-for-meaning"
        ),
        MotivationalQuote(
            id = "quote_mind_02",
            text = "Smile, breathe, and go slowly. There is no need to hurry into the next digital distraction.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Thich Nhat Hanh",
            attributionUrl = "https://plumvillage.org/about/thich-nhat-hanh"
        ),
        MotivationalQuote(
            id = "quote_mind_03",
            text = "Notice the physical impulse to unlock your phone before you obey it.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_mind_04",
            text = "Wherever you are, be there totally.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Eckhart Tolle",
            attributionUrl = "https://eckharttolle.com/the-power-of-now/"
        ),
        MotivationalQuote(
            id = "quote_mind_05",
            text = "Ask yourself before opening any app: What specific intention am I serving right now?",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Catherine Price",
            attributionUrl = "https://phonebreakup.com/"
        ),
        MotivationalQuote(
            id = "quote_mind_06",
            text = "Boredom is not a problem to be cured by a screen; it is the doorway to creative thought.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_mind_07",
            text = "Do not allow the outer world to dictate your inner state.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Marcus Aurelius",
            attributionUrl = "https://en.wikisource.org/wiki/The_Meditations_of_the_Emperor_Marcus_Antoninus"
        ),
        MotivationalQuote(
            id = "quote_mind_08",
            text = "Silence the noise so you can hear the thoughts that actually matter.",
            category = QuoteCategory.MINDFULNESS_INTENTION,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),

        // =========================================================================
        // 4. Discipline & Habit Formation
        // =========================================================================
        MotivationalQuote(
            id = "quote_disc_01",
            text = "You do not rise to the level of your goals. You fall to the level of your systems.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "James Clear",
            attributionUrl = "https://jamesclear.com/atomic-habits"
        ),
        MotivationalQuote(
            id = "quote_disc_02",
            text = "Discipline is choosing between what you want now and what you want most.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Abraham Lincoln",
            attributionUrl = "https://www.goodreads.com/quotes/60117"
        ),
        MotivationalQuote(
            id = "quote_disc_03",
            text = "No man is free who is not master of himself.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Epictetus",
            attributionUrl = "https://en.wikisource.org/wiki/Discourses_of_Epictetus"
        ),
        MotivationalQuote(
            id = "quote_disc_04",
            text = "Small daily habits compound quietly into undeniable life results.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "James Clear",
            attributionUrl = "https://jamesclear.com/continuous-improvement"
        ),
        MotivationalQuote(
            id = "quote_disc_05",
            text = "Friction is the antidote to compulsion. Put distance between impulse and action.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_disc_06",
            text = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Will Durant",
            attributionUrl = "https://en.wikipedia.org/wiki/The_Story_of_Philosophy"
        ),
        MotivationalQuote(
            id = "quote_disc_07",
            text = "A slip in discipline does not erase your trajectory unless you surrender to it.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_disc_08",
            text = "Motivation gets you started; disciplined boundaries keep you free.",
            category = QuoteCategory.DISCIPLINE_HABITS,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),

        // =========================================================================
        // 5. Rest & Being Present
        // =========================================================================
        MotivationalQuote(
            id = "quote_rest_01",
            text = "Almost everything will work again if you unplug it for a few minutes, including you.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Anne Lamott",
            attributionUrl = "https://www.goodreads.com/author/quotes/4329.Anne_Lamott"
        ),
        MotivationalQuote(
            id = "quote_rest_02",
            text = "The time you enjoy wasting without guilt or numbness is not wasted time.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Bertrand Russell",
            attributionUrl = "https://www.goodreads.com/work/quotes/1715424"
        ),
        MotivationalQuote(
            id = "quote_rest_03",
            text = "True rest restores your spirit; endless passive scrolling merely depletes your focus.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_rest_04",
            text = "Look up from the glass screen. The tangible world is rich with texture and color.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_rest_05",
            text = "Be present with the people sitting across from you. They deserve your undivided gaze.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_rest_06",
            text = "Rest is not a reward for finished work; it is an essential human prerequisite for thoughtful living.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Alex Soojung-Kim Pang",
            attributionUrl = "https://www.goodreads.com/book/show/29430725-rest"
        ),
        MotivationalQuote(
            id = "quote_rest_07",
            text = "Solitude is where we find ourselves; algorithmic chatter is where we lose ourselves.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_rest_08",
            text = "A quiet evening without glowing notifications is a sanctuary for restorative sleep.",
            category = QuoteCategory.REST_PRESENCE,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),

        // =========================================================================
        // 6. Digital Balance & Personal Agency
        // =========================================================================
        MotivationalQuote(
            id = "quote_bal_01",
            text = "Technology is a useful servant but a dangerous master.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Christian Lous Lange",
            attributionUrl = "https://www.nobelprize.org/prizes/peace/1921/lange/biographical/"
        ),
        MotivationalQuote(
            id = "quote_bal_02",
            text = "If you are not paying for the product, you and your attention are the product being sold.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Tristan Harris",
            attributionUrl = "https://www.humanetech.com/"
        ),
        MotivationalQuote(
            id = "quote_bal_03",
            text = "Our attention did not collapse; it was stolen by business models optimized for our distraction.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Johann Hari",
            attributionUrl = "https://stolenfocusbook.com/"
        ),
        MotivationalQuote(
            id = "quote_bal_04",
            text = "Do not trade your real life for curated digital highlights of someone else's.",
            category = QuoteCategory.DIGITAL_BALANCE,
            targetContext = "instagram",
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_05",
            text = "Consuming tutorials does not equal building competence. Close the video and write the code.",
            category = QuoteCategory.DIGITAL_BALANCE,
            targetContext = "youtube",
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_06",
            text = "Streaks in an application mean nothing if your real-life discipline streak is fractured.",
            category = QuoteCategory.DIGITAL_BALANCE,
            targetContext = "snapchat",
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_07",
            text = "Leveling up an in-game avatar is comfortable; leveling up your real life creates permanent agency.",
            category = QuoteCategory.DIGITAL_BALANCE,
            targetContext = "gaming",
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_08",
            text = "The scroll is infinite, but your day is finite. Choose agency over algorithmic submission.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_09",
            text = "Digital tools should empower your goals, not siphon your vitality into passive consumption.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Doomscroll Reflection",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
        ),
        MotivationalQuote(
            id = "quote_bal_10",
            text = "You are the author of your day. Step away from the glass and build something tangible.",
            category = QuoteCategory.DIGITAL_BALANCE,
            author = "Doomscroll Philosophy",
            attributionUrl = "https://github.com/jeevansagale/Doom-Scroll-Kill"
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

    /**
     * Selects the next quote for an application session while preventing immediate repetition.
     * Safely handles edge cases like single-item datasets or missing last quote IDs.
     */
    fun getNextSessionQuote(lastQuoteId: String? = null): MotivationalQuote {
        if (allQuotes.isEmpty()) {
            return MotivationalQuote(
                id = "fallback",
                text = "Be mindful of your time.",
                category = QuoteCategory.DIGITAL_BALANCE
            )
        }
        if (allQuotes.size == 1) {
            return allQuotes.first()
        }
        val eligible = allQuotes.filter { it.id != lastQuoteId }
        val pool = eligible.ifEmpty { allQuotes }
        return pool.random()
    }

    fun getQuoteForPackage(packageName: String, excludeId: String? = null): MotivationalQuote {
        if (allQuotes.isEmpty()) {
            return MotivationalQuote(
                id = "fallback",
                text = "Be mindful of your time.",
                category = QuoteCategory.DIGITAL_BALANCE
            )
        }
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

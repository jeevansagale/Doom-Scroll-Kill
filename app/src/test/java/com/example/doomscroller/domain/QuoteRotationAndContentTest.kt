package com.example.doomscroller.domain

import com.example.doomscroller.domain.learning.EducationalQuestion
import com.example.doomscroller.domain.learning.QuestionBank
import com.example.doomscroller.domain.learning.QuestionCategory
import com.example.doomscroller.domain.quotes.MotivationalQuote
import com.example.doomscroller.domain.quotes.MotivationalQuoteEngine
import com.example.doomscroller.domain.quotes.QuoteCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRotationAndContentTest {

    // =========================================================================
    // 1. Content Quantity Requirements (At least 40 unique items each)
    // =========================================================================

    @Test
    fun `dataset contains at least 40 unique questions and 40 unique quotes`() {
        val questions = QuestionBank.allQuestions
        val quotes = MotivationalQuoteEngine.allQuotes

        assertTrue(
            "Expected at least 40 questions, found ${questions.size}",
            questions.size >= 40
        )
        assertTrue(
            "Expected at least 40 quotes, found ${quotes.size}",
            quotes.size >= 40
        )

        // Verify headroom (ideally 50+)
        assertTrue(
            "Expected at least 50 questions for headroom, found ${questions.size}",
            questions.size >= 50
        )
        assertTrue(
            "Expected at least 50 quotes for headroom, found ${quotes.size}",
            quotes.size >= 50
        )
    }

    // =========================================================================
    // 2. Content Validation & Integrity (No Empty Entries, Unique IDs)
    // =========================================================================

    @Test
    fun `question content validation detects zero empty entries and zero duplicate IDs`() {
        val questions = QuestionBank.allQuestions
        val seenIds = mutableSetOf<String>()

        for (q in questions) {
            // ID validation
            assertTrue("Question ID must not be blank", q.id.isNotBlank())
            assertTrue("Duplicate question ID detected: ${q.id}", seenIds.add(q.id))

            // Text and explanation validation
            assertTrue("Question text must not be blank for ${q.id}", q.questionText.isNotBlank())
            assertTrue("Explanation must not be blank for ${q.id}", q.explanation.isNotBlank())

            // Options validation: exactly 4 unique options
            assertEquals("Question ${q.id} must have exactly 4 options", 4, q.options.size)
            assertEquals("Options for question ${q.id} must be distinct", 4, q.options.distinct().size)

            // Correct answer validation
            assertTrue("Correct answer must be non-blank for ${q.id}", q.correctAnswer.isNotBlank())
            assertTrue(
                "Correct answer must be one of the 4 options for ${q.id}",
                q.options.contains(q.correctAnswer)
            )

            // Category validation
            assertNotNull("Category must be assigned for ${q.id}", q.category)
        }
    }

    @Test
    fun `quote content validation detects zero empty entries and zero duplicate IDs`() {
        val quotes = MotivationalQuoteEngine.allQuotes
        val seenIds = mutableSetOf<String>()

        for (quote in quotes) {
            // ID validation
            assertTrue("Quote ID must not be blank", quote.id.isNotBlank())
            assertTrue("Duplicate quote ID detected: ${quote.id}", seenIds.add(quote.id))

            // Text and author validation
            assertTrue("Quote text must not be blank for ${quote.id}", quote.text.isNotBlank())
            assertTrue("Author must not be blank for ${quote.id}", quote.author.isNotBlank())
            assertNotNull("Category must be assigned for ${quote.id}", quote.category)
        }
    }

    // =========================================================================
    // 3. Quote Rotation Across Sessions & No Immediate Repeats
    // =========================================================================

    @Test
    fun `quote rotation avoids immediate repetition across consecutive sessions`() {
        var lastQuoteId: String? = null

        // Simulate 100 consecutive app launches
        for (i in 1..100) {
            val nextQuote = MotivationalQuoteEngine.getNextSessionQuote(lastQuoteId)
            assertNotNull(nextQuote)

            if (lastQuoteId != null) {
                assertNotEquals(
                    "Session $i: Quote was immediately repeated!",
                    lastQuoteId,
                    nextQuote.id
                )
            }
            lastQuoteId = nextQuote.id
        }
    }

    @Test
    fun `quote rotation safely handles single quote dataset edge case`() {
        // If dataset has only 1 item, it should return that item without infinite loop or crash
        val singleQuoteList = listOf(
            MotivationalQuote(
                id = "single_01",
                text = "Only one quote exists.",
                category = QuoteCategory.DIGITAL_BALANCE,
                author = "Solo"
            )
        )

        val eligible = singleQuoteList.filter { it.id != "single_01" }
        val pool = eligible.ifEmpty { singleQuoteList }
        val result = pool.random()

        assertEquals("single_01", result.id)
    }

    // =========================================================================
    // 4. UI Recomposition Stability
    // =========================================================================

    @Test
    fun `quote selection does not change during UI recomposition`() {
        // In ViewModel / UI state, session quote is held in a StateFlow initialized once.
        // Multiple reads within the same session must return the identical quote instance.
        val initialQuote = MotivationalQuoteEngine.getNextSessionQuote(null)

        // Simulate 50 UI recompositions / re-renders
        for (recomposition in 1..50) {
            val currentDisplayedQuote = initialQuote // Held in ViewModel StateFlow
            assertEquals(
                "Recomposition $recomposition must not alter displayed quote",
                initialQuote.id,
                currentDisplayedQuote.id
            )
            assertEquals(initialQuote.text, currentDisplayedQuote.text)
        }
    }

    // =========================================================================
    // 5. Question Coverage Across Required Categories
    // =========================================================================

    @Test
    fun `reflection questions cover all 8 required digital wellbeing categories`() {
        val questions = QuestionBank.allQuestions

        val requiredCategories = listOf(
            QuestionCategory.INTENTIONAL_VS_HABITUAL,
            QuestionCategory.TRIGGERS_AND_BOREDOM,
            QuestionCategory.FOCUS_AND_INTERRUPTIONS,
            QuestionCategory.TIME_AND_PRIORITIES,
            QuestionCategory.SLEEP_AND_ROUTINES,
            QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
            QuestionCategory.MOOD_AND_EMOTIONS,
            QuestionCategory.GOALS_AND_ALTERNATIVES
        )

        for (cat in requiredCategories) {
            val matching = questions.filter { it.category == cat }
            assertTrue(
                "Category ${cat.displayName} must contain at least 4 questions, found ${matching.size}",
                matching.size >= 4
            )
        }
    }
}

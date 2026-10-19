package com.example.doomscroller.domain.learning

enum class QuestionCategory(val displayName: String) {
    // Reflection & Digital Wellbeing categories
    INTENTIONAL_VS_HABITUAL("Intentional vs Habitual Use"),
    TRIGGERS_AND_BOREDOM("Triggers & Boredom"),
    FOCUS_AND_INTERRUPTIONS("Focus & Interruptions"),
    TIME_AND_PRIORITIES("Time Awareness & Priorities"),
    SLEEP_AND_ROUTINES("Sleep & Routines"),
    RELATIONSHIPS_AND_PRESENCE("Relationships & Presence"),
    MOOD_AND_EMOTIONS("Mood & Emotional Regulation"),
    GOALS_AND_ALTERNATIVES("Personal Goals & Offline Alternatives"),

    // Technical categories preserved for backwards compatibility
    DSA("Data Structures & Algorithms"),
    PROGRAMMING("Programming Languages"),
    COMPUTER_SCIENCE("Computer Science Fundamentals"),
    AI_ML("Artificial Intelligence & ML"),
    MATHEMATICS("Discrete Mathematics"),
    LOGICAL_REASONING("Logical Reasoning")
}

enum class QuestionDifficulty {
    EASY, MEDIUM, HARD
}

data class EducationalQuestion(
    val id: String,
    val category: QuestionCategory,
    val difficulty: QuestionDifficulty,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val tags: List<String> = emptyList()
) {
    init {
        require(id.isNotBlank()) { "Question ID must not be blank" }
        require(questionText.isNotBlank()) { "Question text must not be blank" }
        require(options.size == 4) { "Question '$id' must contain exactly 4 options" }
        require(options.distinct().size == 4) { "Options for question '$id' must be distinct" }
        require(options.contains(correctAnswer)) { "Correct answer must be one of the options for question '$id'" }
        require(explanation.isNotBlank()) { "Explanation must not be blank for question '$id'" }
    }

    fun validateAnswer(selectedOption: String): Boolean {
        return selectedOption.trim().equals(correctAnswer.trim(), ignoreCase = false)
    }
}

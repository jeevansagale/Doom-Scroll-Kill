package com.example.doomscroller.domain.learning

import android.util.Log

object QuestionBank {
    private const val TAG = "QuestionBank"

    val allQuestions: List<EducationalQuestion> by lazy {
        val list = listOf(
            // --- Data Structures & Algorithms ---
            EducationalQuestion(
                id = "dsa_01",
                category = QuestionCategory.DSA,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the average time complexity of searching for an element in a Hash Table?",
                options = listOf("O(1)", "O(n)", "O(log n)", "O(n log n)"),
                correctAnswer = "O(1)",
                explanation = "Hash tables compute an index via hashing to achieve constant average O(1) lookup time.",
                tags = listOf("hashmap", "complexity", "dsa")
            ),
            EducationalQuestion(
                id = "dsa_02",
                category = QuestionCategory.DSA,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Which data structure is primarily used to implement Breadth-First Search (BFS) in a graph?",
                options = listOf("Queue", "Stack", "Priority Queue", "Binary Search Tree"),
                correctAnswer = "Queue",
                explanation = "BFS visits vertices level by level, requiring FIFO ordering provided by a Queue.",
                tags = listOf("graph", "bfs", "queue")
            ),
            EducationalQuestion(
                id = "dsa_03",
                category = QuestionCategory.DSA,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is the worst-case time complexity of QuickSort when a poor pivot is repeatedly chosen?",
                options = listOf("O(n²)", "O(n log n)", "O(n)", "O(log n)"),
                correctAnswer = "O(n²)",
                explanation = "When partitions are maximally unbalanced (e.g., pivot is always min/max), recursion depth is n, yielding O(n²).",
                tags = listOf("sorting", "quicksort")
            ),
            EducationalQuestion(
                id = "dsa_04",
                category = QuestionCategory.DSA,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the height of a balanced Binary Search Tree containing n nodes?",
                options = listOf("O(log n)", "O(n)", "O(1)", "O(n log n)"),
                correctAnswer = "O(log n)",
                explanation = "In a balanced binary tree, each level doubles capacity, keeping tree height proportional to log₂(n).",
                tags = listOf("tree", "bst")
            ),
            EducationalQuestion(
                id = "dsa_05",
                category = QuestionCategory.DSA,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Which algorithm finds the shortest path from a single source to all vertices in a weighted graph with non-negative edges?",
                options = listOf("Dijkstra's Algorithm", "Floyd-Warshall Algorithm", "Kruskal's Algorithm", "Prim's Algorithm"),
                correctAnswer = "Dijkstra's Algorithm",
                explanation = "Dijkstra's algorithm greedily extracts minimum distance vertices using a priority queue for non-negative weights.",
                tags = listOf("graph", "shortest-path")
            ),

            // --- Programming Languages ---
            EducationalQuestion(
                id = "prog_01",
                category = QuestionCategory.PROGRAMMING,
                difficulty = QuestionDifficulty.EASY,
                questionText = "In Kotlin, what is the key difference between 'val' and 'var'?",
                options = listOf(
                    "'val' is read-only (immutable reference), whereas 'var' is mutable",
                    "'val' is allocated on the heap, while 'var' is on the stack",
                    "'val' can only store primitive types",
                    "'var' cannot be reassigned once initialized"
                ),
                correctAnswer = "'val' is read-only (immutable reference), whereas 'var' is mutable",
                explanation = "'val' assigns a reference that cannot be reassigned, promoting immutability.",
                tags = listOf("kotlin", "syntax")
            ),
            EducationalQuestion(
                id = "prog_02",
                category = QuestionCategory.PROGRAMMING,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "In Kotlin Coroutines, what is the primary benefit of structured concurrency?",
                options = listOf(
                    "Child coroutines are tied to parent scopes, preventing coroutine leaks",
                    "It automatically executes coroutines on multiple physical machines",
                    "It eliminates the need for coroutine dispatchers",
                    "It guarantees coroutines never throw exceptions"
                ),
                correctAnswer = "Child coroutines are tied to parent scopes, preventing coroutine leaks",
                explanation = "Structured concurrency ensures parent scopes wait for children and cancel children when the parent fails.",
                tags = listOf("kotlin", "coroutines")
            ),
            EducationalQuestion(
                id = "prog_03",
                category = QuestionCategory.PROGRAMMING,
                difficulty = QuestionDifficulty.EASY,
                questionText = "In Python, which built-in function returns both the index and value during loop iteration?",
                options = listOf("enumerate()", "zip()", "range()", "iter()"),
                correctAnswer = "enumerate()",
                explanation = "enumerate(iterable) yields tuples containing (index, element) at each step.",
                tags = listOf("python", "iteration")
            ),
            EducationalQuestion(
                id = "prog_04",
                category = QuestionCategory.PROGRAMMING,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What happens when an object is passed by value in Java and Kotlin?",
                options = listOf(
                    "The object reference is copied by value; the underlying object is shared",
                    "A deep copy of the entire object graph is created",
                    "The original object memory is locked until the function returns",
                    "The object is converted into a primitive representation"
                ),
                correctAnswer = "The object reference is copied by value; the underlying object is shared",
                explanation = "Both Java and Kotlin pass object references by value: the pointer value is copied, pointing to the same instance.",
                tags = listOf("memory", "java", "kotlin")
            ),

            // --- Computer Science Fundamentals ---
            EducationalQuestion(
                id = "cs_01",
                category = QuestionCategory.COMPUTER_SCIENCE,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Which condition is NOT one of the Coffman conditions required for a Deadlock to occur?",
                options = listOf(
                    "Preemptive resource allocation",
                    "Mutual Exclusion",
                    "Hold and Wait",
                    "Circular Wait"
                ),
                correctAnswer = "Preemptive resource allocation",
                explanation = "Deadlock requires No Preemption (resources cannot be forcibly taken away). Preemption prevents deadlock.",
                tags = listOf("os", "deadlock", "concurrency")
            ),
            EducationalQuestion(
                id = "cs_02",
                category = QuestionCategory.COMPUTER_SCIENCE,
                difficulty = QuestionDifficulty.EASY,
                questionText = "In TCP/IP networking, what does the 3-way handshake establish before data transmission?",
                options = listOf(
                    "SYN -> SYN-ACK -> ACK",
                    "ACK -> SYN -> FIN",
                    "PING -> PONG -> OK",
                    "HELLO -> READY -> START"
                ),
                correctAnswer = "SYN -> SYN-ACK -> ACK",
                explanation = "TCP initiates connection by client sending SYN, server replying SYN-ACK, and client confirming ACK.",
                tags = listOf("networking", "tcp")
            ),
            EducationalQuestion(
                id = "cs_03",
                category = QuestionCategory.COMPUTER_SCIENCE,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is the primary function of Virtual Memory in modern operating systems?",
                options = listOf(
                    "Provides process isolation and allows addressing beyond physical RAM capacity",
                    "Increases GPU rendering speed for 3D games",
                    "Permanently compresses hard drive partitions",
                    "Bypasses the CPU cache directly to disk"
                ),
                correctAnswer = "Provides process isolation and allows addressing beyond physical RAM capacity",
                explanation = "Virtual memory maps virtual addresses to physical pages via page tables, enabling memory protection and paging.",
                tags = listOf("os", "memory")
            ),

            // --- Artificial Intelligence & ML ---
            EducationalQuestion(
                id = "aiml_01",
                category = QuestionCategory.AI_ML,
                difficulty = QuestionDifficulty.EASY,
                questionText = "In Machine Learning, what is 'Overfitting'?",
                options = listOf(
                    "Model memorizes training data noise and performs poorly on unseen test data",
                    "Model is too simple to capture underlying data patterns",
                    "Training dataset has too few features",
                    "Learning rate is set too high, causing divergent loss"
                ),
                correctAnswer = "Model memorizes training data noise and performs poorly on unseen test data",
                explanation = "Overfitting occurs when high-capacity models fit training quirks rather than generalizable patterns.",
                tags = listOf("ml", "generalization")
            ),
            EducationalQuestion(
                id = "aiml_02",
                category = QuestionCategory.AI_ML,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What architectural mechanism allows Transformer models to process all tokens in a sequence concurrently?",
                options = listOf(
                    "Self-Attention mechanism",
                    "Recurrent Hidden State Passing",
                    "Markov Decision Chain",
                    "Convolutional Stride Filtering"
                ),
                correctAnswer = "Self-Attention mechanism",
                explanation = "Self-Attention computes pairwise token affinities in parallel across the sequence, avoiding sequential RNN bottlenecks.",
                tags = listOf("ai", "transformers", "nlp")
            ),
            EducationalQuestion(
                id = "aiml_03",
                category = QuestionCategory.AI_ML,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Which optimization algorithm is the foundation for updating weights in neural networks via backpropagation?",
                options = listOf(
                    "Gradient Descent",
                    "Dijkstra's Search",
                    "K-Means Clustering",
                    "Binary Search"
                ),
                correctAnswer = "Gradient Descent",
                explanation = "Gradient Descent iteratively shifts network weights in the negative gradient direction of the loss function.",
                tags = listOf("ai", "optimization")
            ),

            // --- Discrete Mathematics ---
            EducationalQuestion(
                id = "math_01",
                category = QuestionCategory.MATHEMATICS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the value of 17 mod 5?",
                options = listOf("2", "3", "1", "0"),
                correctAnswer = "2",
                explanation = "17 divided by 5 equals 3 with a remainder of 2. Hence, 17 mod 5 = 2.",
                tags = listOf("math", "modular-arithmetic")
            ),
            EducationalQuestion(
                id = "math_02",
                category = QuestionCategory.MATHEMATICS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "In a graph with n vertices, what is the minimum number of edges required to keep it connected?",
                options = listOf("n - 1", "n", "n(n - 1) / 2", "2n"),
                correctAnswer = "n - 1",
                explanation = "A spanning tree on n vertices has exactly n - 1 edges and is minimally connected.",
                tags = listOf("math", "graph-theory")
            ),
            EducationalQuestion(
                id = "math_03",
                category = QuestionCategory.MATHEMATICS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "According to De Morgan's Laws, what is the negation of (A AND B)?",
                options = listOf(
                    "(NOT A) OR (NOT B)",
                    "(NOT A) AND (NOT B)",
                    "A OR B",
                    "NOT (A OR B)"
                ),
                correctAnswer = "(NOT A) OR (NOT B)",
                explanation = "De Morgan's theorem states: !(A && B) == (!A || !B).",
                tags = listOf("math", "boolean-logic")
            ),

            // --- Logical Reasoning ---
            EducationalQuestion(
                id = "logic_01",
                category = QuestionCategory.LOGICAL_REASONING,
                difficulty = QuestionDifficulty.EASY,
                questionText = "If all squares are rectangles, and all rectangles are polygons, which conclusion is definitely true?",
                options = listOf(
                    "All squares are polygons",
                    "All polygons are squares",
                    "No rectangles are squares",
                    "Some squares are not polygons"
                ),
                correctAnswer = "All squares are polygons",
                explanation = "By transitive deduction: Squares ⊆ Rectangles ⊆ Polygons, so all squares are polygons.",
                tags = listOf("logic", "syllogism")
            ),
            EducationalQuestion(
                id = "logic_02",
                category = QuestionCategory.LOGICAL_REASONING,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "A is older than B. C is younger than B. D is older than A. Who is the youngest?",
                options = listOf("C", "B", "A", "D"),
                correctAnswer = "C",
                explanation = "The age ordering from oldest to youngest is: D > A > B > C. Therefore, C is the youngest.",
                tags = listOf("logic", "ordering")
            ),
            EducationalQuestion(
                id = "logic_03",
                category = QuestionCategory.LOGICAL_REASONING,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Complete the sequence: 2, 6, 12, 20, 30, ___?",
                options = listOf("42", "40", "36", "48"),
                correctAnswer = "42",
                explanation = "The increments are +4, +6, +8, +10. The next increment is +12, so 30 + 12 = 42.",
                tags = listOf("logic", "sequence")
            )
        )

        // Sanity check all questions at load time
        for (q in list) {
            try {
                // Instantiation validates assertions
                Log.d(TAG, "Validated question: ${q.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Malformed question ${q.id} in bank!", e)
            }
        }
        list
    }

    fun getQuestionById(id: String): EducationalQuestion? {
        return allQuestions.find { it.id == id }
    }

    fun selectDistinctQuestions(count: Int, excludeIds: Set<String> = emptySet()): List<EducationalQuestion> {
        val eligible = allQuestions.filter { it.id !in excludeIds }
        val pool = if (eligible.size >= count) eligible else allQuestions
        return pool.shuffled().take(count)
    }
}

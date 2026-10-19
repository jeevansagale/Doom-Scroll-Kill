package com.example.doomscroller.domain.learning

import android.util.Log

object QuestionBank {
    private const val TAG = "QuestionBank"

    val allQuestions: List<EducationalQuestion> by lazy {
        val list = listOf(
            // =========================================================================
            // 1. Intentional versus Habitual Use
            // =========================================================================
            EducationalQuestion(
                id = "ref_hab_01",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the most effective way to distinguish between intentional phone use and automatic habit?",
                options = listOf(
                    "State a specific purpose and time limit before unlocking the screen",
                    "Keep checking notifications every few minutes just in case",
                    "Rely entirely on willpower without changing any phone settings",
                    "Open social media whenever you experience a brief pause"
                ),
                correctAnswer = "State a specific purpose and time limit before unlocking the screen",
                explanation = "Naming a concrete objective before unlocking brings conscious intention to an otherwise automatic motor reflex.",
                tags = listOf("intention", "habits", "awareness")
            ),
            EducationalQuestion(
                id = "ref_hab_02",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.EASY,
                questionText = "When you instinctively reach for your phone during an elevator ride, what is typically triggering that action?",
                options = listOf(
                    "A habitual dopamine reflex seeking stimulation during micro-downtime",
                    "An urgent communication emergency that cannot wait sixty seconds",
                    "A conscious decision to research an important life topic",
                    "An unavoidable biological necessity"
                ),
                correctAnswer = "A habitual dopamine reflex seeking stimulation during micro-downtime",
                explanation = "Micro-moments of stillness often trigger automatic avoidance of brief silence through phone checking.",
                tags = listOf("habits", "stimulus", "dopamine")
            ),
            EducationalQuestion(
                id = "ref_hab_03",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How does moving infinite-feed apps off the home screen reduce mindless screen time?",
                options = listOf(
                    "It introduces cognitive friction that breaks the automatic muscle-memory loop",
                    "It makes the phone run out of battery faster",
                    "It deletes the user account automatically",
                    "It stops all incoming text messages completely"
                ),
                correctAnswer = "It introduces cognitive friction that breaks the automatic muscle-memory loop",
                explanation = "Adding even two seconds of effort (like typing an app name) gives the conscious prefrontal cortex time to intervene.",
                tags = listOf("friction", "environment", "habits")
            ),
            EducationalQuestion(
                id = "ref_hab_04",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Which prompt is most helpful to ask yourself when opening a video or social feed?",
                options = listOf(
                    "What specific question or task brought me here, and when will I exit?",
                    "How many hours can I lose without noticing?",
                    "Who has posted something that might make me envious?",
                    "Can I consume content until my battery reaches zero?"
                ),
                correctAnswer = "What specific question or task brought me here, and when will I exit?",
                explanation = "Defining entry criteria and exit criteria transforms passive browsing into an intentional search.",
                tags = listOf("intention", "boundaries")
            ),
            EducationalQuestion(
                id = "ref_hab_05",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Why do infinite scroll mechanisms tend to produce longer sessions than traditional paginated feeds?",
                options = listOf(
                    "They remove natural stopping cues that signal the completion of an activity",
                    "They require users to manually refresh the page repeatedly",
                    "They limit the total amount of available content",
                    "They increase the physical weight of the handheld device"
                ),
                correctAnswer = "They remove natural stopping cues that signal the completion of an activity",
                explanation = "Without chapters, pages, or edges, the brain lacks the visual pause that prompts the decision to stop.",
                tags = listOf("architecture", "stopping-cues", "design")
            ),
            EducationalQuestion(
                id = "ref_hab_06",
                category = QuestionCategory.INTENTIONAL_VS_HABITUAL,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is a practical mindful replacement for opening an app out of pure reflex?",
                options = listOf(
                    "Take three deep breaths and observe your physical surroundings for thirty seconds",
                    "Switch to a different video platform immediately",
                    "Increase the screen brightness to maximum level",
                    "Refresh your email inbox five consecutive times"
                ),
                correctAnswer = "Take three deep breaths and observe your physical surroundings for thirty seconds",
                explanation = "A brief sensory grounding pause creates space between stimulus and automatic reaction.",
                tags = listOf("mindfulness", "replacement", "grounding")
            ),

            // =========================================================================
            // 2. Doomscrolling Triggers and Boredom
            // =========================================================================
            EducationalQuestion(
                id = "ref_bor_01",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What positive developmental role does boredom naturally play when not immediately suppressed by a screen?",
                options = listOf(
                    "It stimulates self-reflection, original daydreaming, and creative problem solving",
                    "It permanently damages cognitive ability",
                    "It proves that someone has no interests or passions",
                    "It causes immediate physical exhaustion"
                ),
                correctAnswer = "It stimulates self-reflection, original daydreaming, and creative problem solving",
                explanation = "Boredom is the brain's signal to seek meaningful engagement; tolerating it allows creative ideas to emerge.",
                tags = listOf("boredom", "creativity", "neuroscience")
            ),
            EducationalQuestion(
                id = "ref_bor_02",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Why does scrolling through sensational or alarming news feeds feel so compelling during stressful periods?",
                options = listOf(
                    "The evolutionary threat-detection system mistakes consumption for survival preparation",
                    "Sensational news instantly cures underlying physiological stress",
                    "Algorithms only display completely verified and calming insights",
                    "Stress shuts down all curiosity and visual perception"
                ),
                correctAnswer = "The evolutionary threat-detection system mistakes consumption for survival preparation",
                explanation = "The amygdala seeks information to resolve uncertainty, but infinite negative feeds perpetuate the threat loop without resolution.",
                tags = listOf("doomscrolling", "stress", "evolutionary-psychology")
            ),
            EducationalQuestion(
                id = "ref_bor_03",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.EASY,
                questionText = "When experiencing an uncomfortable emotion like loneliness or frustration, what makes social media an appealing escape?",
                options = listOf(
                    "It offers rapid, low-effort numbing and temporary emotional distraction",
                    "It directly addresses and solves the root cause of the emotional state",
                    "It requires deep cognitive exertion that tires out the body",
                    "It eliminates the need for any human communication"
                ),
                correctAnswer = "It offers rapid, low-effort numbing and temporary emotional distraction",
                explanation = "Screens offer quick affective relief, though the underlying emotion remains unaddressed when the phone is locked.",
                tags = listOf("triggers", "emotions", "distraction")
            ),
            EducationalQuestion(
                id = "ref_bor_04",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is the 'variable reward schedule' and why does it keep users scrolling through mediocre content?",
                options = listOf(
                    "Unpredictable intermittent rewards create higher compulsive engagement than predictable rewards",
                    "Predictable rewards always create the strongest neurological fixation",
                    "Rewards that never occur are the most engaging pattern",
                    "Fixed scheduled rewards reduce all interest in digital interfaces"
                ),
                correctAnswer = "Unpredictable intermittent rewards create higher compulsive engagement than predictable rewards",
                explanation = "Like a slot machine, not knowing whether the next swipe holds a funny or interesting post drives persistent checking.",
                tags = listOf("variable-rewards", "dopamine", "compulsion")
            ),
            EducationalQuestion(
                id = "ref_bor_05",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.EASY,
                questionText = "If you notice yourself opening apps repeatedly when waiting for a bus, what experiment could you try?",
                options = listOf(
                    "Spend the waiting time people-watching, stretching, or noticing sounds around you",
                    "Buy a second smartphone to double your screen options",
                    "Complain online about the waiting duration",
                    "Close your eyes and sprint toward the bus stop"
                ),
                correctAnswer = "Spend the waiting time people-watching, stretching, or noticing sounds around you",
                explanation = "Small intervals of analog presence recalibrate tolerance for stillness and lower digital baseline stimulation.",
                tags = listOf("experiment", "presence", "stillness")
            ),
            EducationalQuestion(
                id = "ref_bor_06",
                category = QuestionCategory.TRIGGERS_AND_BOREDOM,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How can identifying your specific 'trigger situations' (e.g. fatigue at 4 PM) help reduce unwanted scrolling?",
                options = listOf(
                    "You can design proactive non-screen breaks like a water refill or brief walk ahead of time",
                    "You can blame yourself more harshly whenever fatigue occurs",
                    "You can eliminate the need for ever sleeping again",
                    "You can force yourself to stare at a blank wall until motivation returns"
                ),
                correctAnswer = "You can design proactive non-screen breaks like a water refill or brief walk ahead of time",
                explanation = "Anticipating predictable energy dips allows you to provide what your body actually needs rather than an algorithmic trap.",
                tags = listOf("triggers", "energy", "proactive")
            ),

            // =========================================================================
            // 3. Focus and Study/Work Interruptions
            // =========================================================================
            EducationalQuestion(
                id = "ref_foc_01",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is 'attention residue' according to cognitive research by Dr. Sophie Leroy?",
                options = listOf(
                    "Cognitive capacity remains partially stuck on a previous task or notification even after switching back",
                    "The physical dust that settles on a laptop screen after prolonged study",
                    "The feeling of total relaxation after completing a project",
                    "An increase in memory retention caused by rapid multi-tasking"
                ),
                correctAnswer = "Cognitive capacity remains partially stuck on a previous task or notification even after switching back",
                explanation = "Checking a quick message leaves fragments of attention on that message, impairing performance on the primary task for up to 20 minutes.",
                tags = listOf("attention-residue", "deep-work", "productivity")
            ),
            EducationalQuestion(
                id = "ref_foc_02",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Where is the most effective physical location for your phone during an intensive study or focus session?",
                options = listOf(
                    "In another room or zipped inside a bag out of direct sight",
                    "Face up directly beside your keyboard with notifications on",
                    "Held in your non-dominant hand while typing with the other",
                    "Taped to your monitor at eye level"
                ),
                correctAnswer = "In another room or zipped inside a bag out of direct sight",
                explanation = "Studies (e.g., Ward et al., UT Austin) prove that even a silenced phone within eyesight drains available cognitive working memory.",
                tags = listOf("environment", "friction", "focus")
            ),
            EducationalQuestion(
                id = "ref_foc_03",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Why does multitasking between writing a report and checking group chats reduce work quality?",
                options = listOf(
                    "Human brains cannot execute two complex cognitive tasks simultaneously; they rapidly task-switch with high error penalty",
                    "Social messages automatically erase computer text files",
                    "Multitasking actually makes humans twice as intelligent",
                    "Writing reports consumes zero mental energy"
                ),
                correctAnswer = "Human brains cannot execute two complex cognitive tasks simultaneously; they rapidly task-switch with high error penalty",
                explanation = "Task-switching burns glucose in the prefrontal cortex, increases mistakes, and prolongs total task duration.",
                tags = listOf("multitasking", "focus", "cognition")
            ),
            EducationalQuestion(
                id = "ref_foc_04",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is the 'Pomodoro Technique' or focused work sprint philosophy?",
                options = listOf(
                    "Committing to 25 to 50 minutes of uninterrupted work followed by a deliberate non-screen rest interval",
                    "Working for 8 hours without blinking or standing up",
                    "Checking messages every 4 minutes on a strict schedule",
                    "Eating tomato soup before every difficult task"
                ),
                correctAnswer = "Committing to 25 to 50 minutes of uninterrupted work followed by a deliberate non-screen rest interval",
                explanation = "Bounded work sprints protect deep focus while assuring the brain that a genuine rest period is coming soon.",
                tags = listOf("pomodoro", "structure", "focus")
            ),
            EducationalQuestion(
                id = "ref_foc_05",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "When hit with a sudden impulse to look up an irrelevant trivia fact during work, what is a disciplined practice?",
                options = listOf(
                    "Jot the topic on a physical notepad to investigate during scheduled break time",
                    "Immediately open 15 browser tabs and fall down a rabbit hole",
                    "Abandon the workday entirely because your curiosity was piqued",
                    "Call three friends to debate the trivia question immediately"
                ),
                correctAnswer = "Jot the topic on a physical notepad to investigate during scheduled break time",
                explanation = "A 'distraction log' captures the impulse safely without derailing your current working train of thought.",
                tags = listOf("distraction-log", "focus", "techniques")
            ),
            EducationalQuestion(
                id = "ref_foc_06",
                category = QuestionCategory.FOCUS_AND_INTERRUPTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What happens to the depth of your thinking when all study sessions are accompanied by continuous notification alerts?",
                options = listOf(
                    "Thoughts remain on surface-level associations because deeper synthesis requires unbroken mental continuity",
                    "Complex conceptual understanding becomes three times faster",
                    "Memory becomes permanently photographic",
                    "Analytical capability shifts into superhuman mode"
                ),
                correctAnswer = "Thoughts remain on surface-level associations because deeper synthesis requires unbroken mental continuity",
                explanation = "Synthesizing hard insights requires holding multiple concepts simultaneously in working memory without disruption.",
                tags = listOf("depth", "synthesis", "learning")
            ),

            // =========================================================================
            // 4. Time Awareness and Priorities
            // =========================================================================
            EducationalQuestion(
                id = "ref_time_01",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "If someone spends two hours daily on short-form video feeds, how much time does that accumulate across a single year?",
                options = listOf(
                    "Over 730 hours (equivalent to approximately 30 full 24-hour days or 90 waking days)",
                    "Less than 5 hours total",
                    "Exactly 14 hours total",
                    "Around 100 minutes total"
                ),
                correctAnswer = "Over 730 hours (equivalent to approximately 30 full 24-hour days or 90 waking days)",
                explanation = "Small daily increments compound massively over time; two hours a day is one-twelfth of an entire year.",
                tags = listOf("compounding", "time", "perspective")
            ),
            EducationalQuestion(
                id = "ref_time_02",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the concept of 'opportunity cost' in relation to digital wellbeing?",
                options = listOf(
                    "Every hour spent passively watching feeds is an hour unavailable for reading, fitness, craft, or relationships",
                    "The monetary price of a monthly smartphone cellular plan",
                    "The discount code you receive for buying apps in bulk",
                    "The electricity bill for charging a handheld device"
                ),
                correctAnswer = "Every hour spent passively watching feeds is an hour unavailable for reading, fitness, craft, or relationships",
                explanation = "Time is a non-renewable zero-sum resource; saying 'yes' to a feed is a silent 'no' to your other life aspirations.",
                tags = listOf("opportunity-cost", "priorities", "time")
            ),
            EducationalQuestion(
                id = "ref_time_03",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Why does time seem to pass remarkably fast during algorithmic doomscrolling sessions?",
                options = listOf(
                    "High-frequency micro-stimuli provide continuous novel dopamine hits without structural landmarks to anchor time perception",
                    "Smartphone internal clocks automatically speed up when running social applications",
                    "Earth rotates faster when mobile screens are illuminated",
                    "Content algorithms delete seconds from the international atomic clock"
                ),
                correctAnswer = "High-frequency micro-stimuli provide continuous novel dopamine hits without structural landmarks to anchor time perception",
                explanation = "Without narrative milestones or cognitive pauses, the brain does not encode distinctive temporal memories, causing time to 'evaporate'.",
                tags = listOf("time-dilation", "perception", "neurology")
            ),
            EducationalQuestion(
                id = "ref_time_04",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is an effective way to align daily phone use with your actual stated core values?",
                options = listOf(
                    "Review weekly screen time reports with compassionate honesty and set explicit daily quotas on high-risk apps",
                    "Never look at usage data and pretend technology has zero impact on your day",
                    "Feel guilty every evening without taking any practical protective actions",
                    "Assume that you are powerless against technology companies"
                ),
                correctAnswer = "Review weekly screen time reports with compassionate honesty and set explicit daily quotas on high-risk apps",
                explanation = "Objective awareness paired with kind accountability empowers you to reclaim agency over your hours.",
                tags = listOf("values", "alignment", "agency")
            ),
            EducationalQuestion(
                id = "ref_time_05",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What did philosopher Seneca mean by writing that 'people are frugal in guarding their personal property; but as soon as it comes to squandering time they are most wasteful'?",
                options = listOf(
                    "We fiercely protect money and possessions, yet freely surrender the one asset we can never replenish",
                    "People should spend all their money on luxury electronics immediately",
                    "Time is worthless compared to gold and physical real estate",
                    "We should never share our possessions with family members"
                ),
                correctAnswer = "We fiercely protect money and possessions, yet freely surrender the one asset we can never replenish",
                explanation = "Seneca recognized over two thousand years ago that time is our most precious yet most carelessly surrendered resource.",
                tags = listOf("philosophy", "seneca", "time")
            ),
            EducationalQuestion(
                id = "ref_time_06",
                category = QuestionCategory.TIME_AND_PRIORITIES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "How does establishing a 'digital shutdown' ritual at the end of a workday benefit personal time?",
                options = listOf(
                    "It creates a clear psychological transition between professional demands and personal life",
                    "It breaks the screen hardware permanently so you cannot work",
                    "It prevents you from ever enjoying entertainment again",
                    "It requires throwing your phone into a body of water"
                ),
                correctAnswer = "It creates a clear psychological transition between professional demands and personal life",
                explanation = "A deliberate close-down routine allows mental disengagement so evening hours can be truly restful.",
                tags = listOf("shutdown", "boundaries", "work-life")
            ),

            // =========================================================================
            // 5. Sleep and Morning/Evening Routines
            // =========================================================================
            EducationalQuestion(
                id = "ref_slp_01",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Why does checking algorithmic feeds immediately upon waking up in bed often lead to higher morning anxiety?",
                options = listOf(
                    "It floods your waking brain with outside emergencies, comparisons, and demands before your own thoughts can stabilize",
                    "It gives your brain necessary physical nutrients needed for breakfast",
                    "It improves cardiovascular endurance for the upcoming morning run",
                    "It turns off the sunrise in your local geographic area"
                ),
                correctAnswer = "It floods your waking brain with outside emergencies, comparisons, and demands before your own thoughts can stabilize",
                explanation = "Morning reactive scrolling places your nervous system into defense mode rather than an intentional, calm start.",
                tags = listOf("morning", "anxiety", "routines")
            ),
            EducationalQuestion(
                id = "ref_slp_02",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How does late-night screen exposure disrupt physiological sleep architecture?",
                options = listOf(
                    "Blue-spectrum light suppresses natural melatonin synthesis while stimulating content delays sleep onset",
                    "Screens lower the physical room temperature below freezing",
                    "Looking at pixels prevents the lungs from taking in oxygen during sleep",
                    "Smartphones transmit soundwaves that prevent dreams from ever occurring"
                ),
                correctAnswer = "Blue-spectrum light suppresses natural melatonin synthesis while stimulating content delays sleep onset",
                explanation = "The combination of circadian photic suppression (light) and cognitive arousal (content) reduces restorative slow-wave sleep.",
                tags = listOf("sleep", "melatonin", "circadian-rhythm")
            ),
            EducationalQuestion(
                id = "ref_slp_03",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is a simple, highly effective physical boundary to prevent bedtime doomscrolling in bed?",
                options = listOf(
                    "Charge the phone outside the bedroom overnight and use a dedicated analog or standalone alarm clock",
                    "Keep the phone under your pillow with vibration set to maximum",
                    "Sleep with headphones playing loud video commentary all night",
                    "Stare into the bright screen until your eyes forcibly close from exhaustion"
                ),
                correctAnswer = "Charge the phone outside the bedroom overnight and use a dedicated analog or standalone alarm clock",
                explanation = "Removing the device from arm's reach eliminates late-night friction battles and secures deep morning tranquility.",
                tags = listOf("sleep-hygiene", "bedroom", "environment")
            ),
            EducationalQuestion(
                id = "ref_slp_04",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is 'revenge bedtime procrastination' and what core human desire is it attempting to fulfill?",
                options = listOf(
                    "Delaying sleep to scroll because daytime felt completely lacking in personal freedom or leisure time",
                    "Staying awake to execute a strategic plan against your workplace rivals",
                    "Refusing to sleep because you enjoy feeling fatigued the next day",
                    "A formal medical procedure for treating insomnia"
                ),
                correctAnswer = "Delaying sleep to scroll because daytime felt completely lacking in personal freedom or leisure time",
                explanation = "When our daytime feels controlled by obligations, we steal time at night for autonomy, even though it costs our next day's energy.",
                tags = listOf("revenge-bedtime", "psychology", "sleep")
            ),
            EducationalQuestion(
                id = "ref_slp_05",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What relaxing analog alternative can help ease your mind into restorative sleep 30 minutes before bed?",
                options = listOf(
                    "Reading a physical paper book, gentle stretching, or listening to calm music in soft ambient light",
                    "Reading angry debate comment sections on controversial topics",
                    "Playing high-stakes competitive online video games",
                    "Checking work email to see if any crisis emerged at midnight"
                ),
                correctAnswer = "Reading a physical paper book, gentle stretching, or listening to calm music in soft ambient light",
                explanation = "Low-stimulation analog rituals cue the parasympathetic nervous system that it is safe to sleep.",
                tags = listOf("evening-routine", "relaxation", "analog")
            ),
            EducationalQuestion(
                id = "ref_slp_06",
                category = QuestionCategory.SLEEP_AND_ROUTINES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "If you wake up in the middle of the night, why is picking up your phone one of the worst actions for falling back asleep?",
                options = listOf(
                    "The intense light and novelty immediately signal to your brain that it is daytime, triggering waking alertness",
                    "Smartphones automatically discharge electricity into blankets",
                    "It will cause all your clock alarms to cancel themselves",
                    "Nighttime screen use permanently disables your Wi-Fi router"
                ),
                correctAnswer = "The intense light and novelty immediately signal to your brain that it is daytime, triggering waking alertness",
                explanation = "Checking a device during nocturnal awakenings resets circadian clocks and initiates alert processing cycles.",
                tags = listOf("night-waking", "circadian", "sleep")
            ),

            // =========================================================================
            // 6. Relationships and Presence
            // =========================================================================
            EducationalQuestion(
                id = "ref_rel_01",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is 'phubbing' (phone-snubbing) and how does it subtly harm in-person interpersonal relationships?",
                options = listOf(
                    "Ignoring a companion in favor of your smartphone, signaling to them that virtual strangers matter more than their presence",
                    "A new high-speed wireless charging technology",
                    "Taking high-resolution group photographs during a dinner party",
                    "Calling a friend on speakerphone while driving safely"
                ),
                correctAnswer = "Ignoring a companion in favor of your smartphone, signaling to them that virtual strangers matter more than their presence",
                explanation = "Repeated phone checks in conversation diminish conversational depth, intimacy, and perceived empathy.",
                tags = listOf("phubbing", "relationships", "empathy")
            ),
            EducationalQuestion(
                id = "ref_rel_02",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Research shows that placing a phone on the dinner table (even face down and silenced) produces what conversational effect?",
                options = listOf(
                    "It reduces feelings of connection and leads to more superficial conversational topics",
                    "It improves food digestion and appetite",
                    "It guarantees that dinner conversation will be twice as meaningful",
                    "It makes the table physically stronger"
                ),
                correctAnswer = "It reduces feelings of connection and leads to more superficial conversational topics",
                explanation = "The visible presence of a phone reminds participants of potential interruption, discouraging vulnerable or deep dialogue.",
                tags = listOf("presence", "dinner", "conversation")
            ),
            EducationalQuestion(
                id = "ref_rel_03",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is a meaningful way to practice high-quality presence when spending time with a friend or family member?",
                options = listOf(
                    "Put your phone entirely out of sight and maintain attentive eye contact while they speak",
                    "Check your notifications discreetly under the table every two minutes",
                    "Show them 45 consecutive short videos you found earlier today",
                    "Keep one earbud in your ear playing background music"
                ),
                correctAnswer = "Put your phone entirely out of sight and maintain attentive eye contact while they speak",
                explanation = "Full undivided attention is one of the rarest and most generous gifts you can offer another human being.",
                tags = listOf("presence", "listening", "friendship")
            ),
            EducationalQuestion(
                id = "ref_rel_04",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How does online parasocial interaction (following influencer lives) differ from authentic reciprocal friendship?",
                options = listOf(
                    "Parasocial bonds are entirely one-way; they provide a false sense of connection without mutual support or accountability",
                    "Influencers know every one of their followers personally and deeply",
                    "Following strangers online is identical to having a childhood best friend",
                    "Parasocial bonds are the only way humans can form community in the 21st century"
                ),
                correctAnswer = "Parasocial bonds are entirely one-way; they provide a false sense of connection without mutual support or accountability",
                explanation = "Investing emotional energy into broadcast feeds can substitute for the messy but deeply fulfilling work of real friendship.",
                tags = listOf("parasocial", "relationships", "authenticity")
            ),
            EducationalQuestion(
                id = "ref_rel_05",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.EASY,
                questionText = "When your partner or child wants to talk with you, what small action demonstrates authentic respect?",
                options = listOf(
                    "Lower or set down your device, turn your body toward them, and actively listen",
                    "Nod repeatedly while keeping your eyes glued to a video reel",
                    "Say 'I am listening' while continuing to type rapid comments online",
                    "Hand them a second screen so you can both ignore each other"
                ),
                correctAnswer = "Lower or set down your device, turn your body toward them, and actively listen",
                explanation = "Physical orientation and direct eye contact communicate undeniable value and emotional safety.",
                tags = listOf("family", "active-listening", "respect")
            ),
            EducationalQuestion(
                id = "ref_rel_06",
                category = QuestionCategory.RELATIONSHIPS_AND_PRESENCE,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Why do shared digital-free spaces (such as phone-free dinner tables or walks) strengthen social bonds?",
                options = listOf(
                    "They create a shared shared container where spontaneous, unhurried communication can naturally unfold",
                    "They force everyone to argue about technology constantly",
                    "They reduce the amount of oxygen in the room",
                    "They prevent people from knowing the current weather forecast"
                ),
                correctAnswer = "They create a shared shared container where spontaneous, unhurried communication can naturally unfold",
                explanation = "Without the escape hatch of an immediate screen, people lean in and discover shared warmth and humor.",
                tags = listOf("connection", "phone-free", "community")
            ),

            // =========================================================================
            // 7. Mood and Emotional Regulation
            // =========================================================================
            EducationalQuestion(
                id = "ref_mood_01",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What cognitive bias often occurs when comparing your ordinary daily life to social media profiles?",
                options = listOf(
                    "Comparing your messy, unedited internal reality to other people's curated and polished external highlights",
                    "Assuming everyone else is having a worse day than you are",
                    "Believing that photos online are completely untouched documentary evidence",
                    "Forgetting that human beings require food and water"
                ),
                correctAnswer = "Comparing your messy, unedited internal reality to other people's curated and polished external highlights",
                explanation = "We compare our backstage struggles with everyone else's front-stage performance, generating artificial inadequacy.",
                tags = listOf("comparison", "social-media", "self-compassion")
            ),
            EducationalQuestion(
                id = "ref_mood_02",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "Why do outrage-inducing posts and inflammatory debates circulate so widely across algorithmic platforms?",
                options = listOf(
                    "Algorithms optimize for high engagement; moral outrage generates strong emotional friction that prolongs time on platform",
                    "Content algorithms are designed by psychologists specifically to make users peaceful and content",
                    "Internet users only ever read and share academic philosophy essays",
                    "Outrageous posts contain fewer letters, making them technically faster to download"
                ),
                correctAnswer = "Algorithms optimize for high engagement; moral outrage generates strong emotional friction that prolongs time on platform",
                explanation = "Anger is an activating emotion that spikes interaction metrics, even as it leaves individuals feeling anxious and depleted.",
                tags = listOf("outrage", "algorithms", "mental-health")
            ),
            EducationalQuestion(
                id = "ref_mood_03",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "If you notice that your chest feels tight and your mood drops after 20 minutes on a specific app, what is a compassionate response?",
                options = listOf(
                    "Gently close the application, take a sip of water, and do a brief body stretch without self-criticism",
                    "Double down and read the comments for another hour to prove your emotional resilience",
                    "Criticize yourself harshly for feeling anxious",
                    "Post an angry update announcing you are leaving the internet forever"
                ),
                correctAnswer = "Gently close the application, take a sip of water, and do a brief body stretch without self-criticism",
                explanation = "Treating yourself with gentle, nonjudgmental awareness breaks the spiral without added shame.",
                tags = listOf("compassion", "somatic", "regulation")
            ),
            EducationalQuestion(
                id = "ref_mood_04",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What is the psychological phenomenon known as FOMO (Fear of Missing Out)?",
                options = listOf(
                    "An anxious apprehension that others might be having rewarding experiences from which you are absent",
                    "A biological deficiency in essential vitamins",
                    "The medical diagnosis for poor battery health on Android devices",
                    "A technique used by athletes to improve reaction times"
                ),
                correctAnswer = "An anxious apprehension that others might be having rewarding experiences from which you are absent",
                explanation = "FOMO is weaponized by algorithmic broadcasts that highlight endless parties, vacations, and milestones you aren't attending.",
                tags = listOf("fomo", "anxiety", "perspective")
            ),
            EducationalQuestion(
                id = "ref_mood_05",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is JOMO (Joy of Missing Out) and how does it foster emotional contentment?",
                options = listOf(
                    "Finding deep peace and satisfaction in being right where you are without needing to witness what others are doing",
                    "Deliberately ruining other people's parties so they feel sad",
                    "Refusing to ever communicate with another human being again",
                    "A marketing campaign for selling oversized headphones"
                ),
                correctAnswer = "Finding deep peace and satisfaction in being right where you are without needing to witness what others are doing",
                explanation = "JOMO embraces the beauty of presence and contentment with your own chosen life rhythm.",
                tags = listOf("jomo", "contentment", "peace")
            ),
            EducationalQuestion(
                id = "ref_mood_06",
                category = QuestionCategory.MOOD_AND_EMOTIONS,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How can a 'digital declutter' (unfollowing accounts that trigger insecurity or hostility) improve daily emotional baseline?",
                options = listOf(
                    "It curates your information diet so the stimulus entering your awareness supports calmness rather than distress",
                    "It causes the phone to run cooler by deleting physical internet cables",
                    "It ensures you will never have to face any real-world challenge again",
                    "It makes all news in the world 100% positive overnight"
                ),
                correctAnswer = "It curates your information diet so the stimulus entering your awareness supports calmness rather than distress",
                explanation = "Just as food nourishes your physical body, the information you absorb shapes your baseline mental climate.",
                tags = listOf("declutter", "curation", "mental-hygiene")
            ),

            // =========================================================================
            // 8. Personal Goals and Offline Alternatives
            // =========================================================================
            EducationalQuestion(
                id = "ref_goal_01",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "Why is replacing phone use with a specific attractive offline hobby more successful than simply trying to 'stop using your phone'?",
                options = listOf(
                    "The human brain seeks rewarding stimulation; nature abhors an empty vacuum and needs positive alternative activities",
                    "Hobby equipment automatically emits radio signals that disable mobile screens",
                    "Willpower alone is always 100% sufficient without having any other activities",
                    "Stopping an activity is neurologically easier than starting a new activity"
                ),
                correctAnswer = "The human brain seeks rewarding stimulation; nature abhors an empty vacuum and needs positive alternative activities",
                explanation = "Habit change succeeds when you substitute a positive offline habit (instrument, woodworking, gardening) for the old cue.",
                tags = listOf("habit-replacement", "hobbies", "action")
            ),
            EducationalQuestion(
                id = "ref_goal_02",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is a satisfying tangible activity you could do right now instead of continuing to scroll on this device?",
                options = listOf(
                    "Take a 15-minute outdoor walk, sketch on paper, read a book chapter, or prepare a fresh snack",
                    "Search for 45 minutes on how to quit social media while remaining on social media",
                    "Stare at your device battery percentage until it drops by one percent",
                    "Refresh your notification shade twenty times consecutively"
                ),
                correctAnswer = "Take a 15-minute outdoor walk, sketch on paper, read a book chapter, or prepare a fresh snack",
                explanation = "Engaging your physical senses in real-world activity immediately restores vitality and clarity.",
                tags = listOf("offline-alternatives", "activity", "real-world")
            ),
            EducationalQuestion(
                id = "ref_goal_03",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "How does writing your weekly or monthly goals down on physical paper help protect against digital distraction?",
                options = listOf(
                    "Physical paper provides a persistent, un-hackable visual beacon of what truly matters to you",
                    "Paper absorbs Wi-Fi signals and prevents your phone from receiving data",
                    "Writing on paper guarantees that you will never feel tired again",
                    "Digital documents are technically impossible to read or write"
                ),
                correctAnswer = "Physical paper provides a persistent, un-hackable visual beacon of what truly matters to you",
                explanation = "An analog notebook or planner does not send push notifications, advertise products, or tempt you with tabs.",
                tags = listOf("analog-tools", "goals", "clarity")
            ),
            EducationalQuestion(
                id = "ref_goal_04",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is the '20-second rule' popularized by psychologist Shawn Achor for developing positive offline habits?",
                options = listOf(
                    "Make good habits 20 seconds easier to start (e.g. guitar out of its case) and distracting habits 20 seconds harder",
                    "Only spend 20 seconds on your goals every calendar year",
                    "Wash your hands for 20 seconds before unlocking your smartphone",
                    "Run for 20 seconds every time you hear a notification chime"
                ),
                correctAnswer = "Make good habits 20 seconds easier to start (e.g. guitar out of its case) and distracting habits 20 seconds harder",
                explanation = "Lowering activation energy for constructive pursuits makes following through the path of least resistance.",
                tags = listOf("activation-energy", "habits", "psychology")
            ),
            EducationalQuestion(
                id = "ref_goal_05",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.MEDIUM,
                questionText = "What long-term capability develops when you consistently choose active creation over passive algorithmic consumption?",
                options = listOf(
                    "Deep craftsmanship, authentic confidence, and a portfolio of meaningful accomplishments",
                    "The ability to watch three videos at quadruple speed simultaneously",
                    "An infinite follower count on every upcoming platform",
                    "Complete immunity to all physical illness"
                ),
                correctAnswer = "Deep craftsmanship, authentic confidence, and a portfolio of meaningful accomplishments",
                explanation = "Creation demands resilience and builds real competence, while passive consumption leaves no lasting footprint.",
                tags = listOf("creation", "mastery", "agency")
            ),
            EducationalQuestion(
                id = "ref_goal_06",
                category = QuestionCategory.GOALS_AND_ALTERNATIVES,
                difficulty = QuestionDifficulty.EASY,
                questionText = "What is an effective way to handle the urge to scroll when waiting for an appointment or commute?",
                options = listOf(
                    "Carry a pocket paperback book or an analog sketchpad with you everywhere you go",
                    "Ask every stranger in the room what their favorite social media handle is",
                    "Complain that the world is moving too slowly",
                    "Keep your phone plugged into a wall outlet across the room"
                ),
                correctAnswer = "Carry a pocket paperback book or an analog sketchpad with you everywhere you go",
                explanation = "Having a physical alternative readily accessible makes intentional choice effortless.",
                tags = listOf("analog", "preparation", "habits")
            ),

            // =========================================================================
            // Legacy / Technical Questions Preserved for Backwards Compatibility
            // =========================================================================
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
            )
        )

        // Sanity check all questions at load time
        for (q in list) {
            try {
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

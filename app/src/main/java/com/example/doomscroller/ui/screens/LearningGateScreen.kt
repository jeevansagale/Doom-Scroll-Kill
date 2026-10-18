package com.example.doomscroller.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doomscroller.domain.learning.QuestionBank
import com.example.doomscroller.domain.quotes.MotivationalQuoteEngine
import com.example.doomscroller.ui.AppViewModel
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow
import kotlinx.coroutines.delay

@Composable
fun LearningGateScreen(
    packageName: String,
    viewModel: AppViewModel,
    onLeaveAppClick: () -> Unit,
    onGateCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back navigation so user cannot back-button into the blocked application
    BackHandler {
        onLeaveAppClick()
    }

    val context = LocalContext.current
    var session by remember(packageName) { mutableStateOf(viewModel.getOrInitLearningSession(packageName)) }
    val appName = remember(packageName) { viewModel.getAppName(packageName) }

    // Active Quote (persisted per session)
    val quote = remember(session.activeQuoteId) {
        session.activeQuoteId?.let { MotivationalQuoteEngine.getQuoteById(it) }
            ?: MotivationalQuoteEngine.getQuoteForPackage(packageName)
    }

    // Timer ticker for penalty cooldown countdown
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val penaltyEndTime = session.enforcementEndTimeMillis
    val isPenaltyActive = session.isEnforcementActive && currentTime < penaltyEndTime
    val remainingMillis = if (isPenaltyActive) (penaltyEndTime - currentTime).coerceAtLeast(0L) else 0L
    val remainingMinutes = (remainingMillis / 1000) / 60
    val remainingSeconds = (remainingMillis / 1000) % 60
    val cooldownFormatted = String.format("%02d:%02d", remainingMinutes, remainingSeconds)

    // Check if learning gate is completed (2 questions answered correctly)
    val isGateFinished = session.isLearningGateCompleted || session.answeredQuestions.size >= 2

    // Current question selection
    val activeQuestion = remember(session.answeredQuestions.size, session.learningQuestions) {
        val qList = session.learningQuestions.mapNotNull { QuestionBank.getQuestionById(it) }
        val answeredCount = session.answeredQuestions.size
        if (answeredCount < qList.size) {
            qList[answeredCount]
        } else {
            qList.lastOrNull() ?: QuestionBank.allQuestions.first()
        }
    }

    // Stable option randomization per question ID
    val shuffledOptions = remember(activeQuestion.id) {
        activeQuestion.options.shuffled()
    }

    var selectedOption by remember(activeQuestion.id) { mutableStateOf<String?>(null) }
    var feedbackMessage by remember(activeQuestion.id) { mutableStateOf<String?>(null) }
    var isIncorrectAnswer by remember(activeQuestion.id) { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stark Bauhaus Top Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BauhausBlack)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DOOMSCROLL LEARNING GATE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isGateFinished) BauhausBlue else BauhausYellow)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target App and Cooldown Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RESTRICTED APPLICATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = appName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausRed
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .background(if (isPenaltyActive) BauhausRed else BauhausBlue)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isPenaltyActive) "COOLDOWN: $cooldownFormatted" else "COOLDOWN ELAPSED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contextual Motivational Quote Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BauhausMuted),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "“${quote.text}”",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = BauhausBlack,
                        lineHeight = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "— ${quote.author.uppercase()} [${quote.category.displayName.uppercase()}]",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = BauhausBlue,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isGateFinished) {
            // --- REFLECTION SCREEN ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(3.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(BauhausBlue)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "2 / 2 QUESTIONS COMPLETED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "REFLECTION",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausBlack,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mandated Reflection Text
                    Text(
                        text = "You could be learning something useful, building your skills, or working toward your goals. You chose to spend this time scrolling. Your next decision is still yours to make.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp,
                            color = BauhausBlack
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dual condition status feedback
                    if (isPenaltyActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausMuted)
                                .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "COOLDOWN STILL ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$cooldownFormatted REMAINING",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausRed
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Restriction will release once cooldown finishes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausYellow)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BOTH CONDITIONS MET: RESTRICTION READY TO RELEASE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlack
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (!isPenaltyActive) {
                Button(
                    onClick = {
                        val released = viewModel.checkAndReleaseRestriction(packageName)
                        if (released) {
                            onGateCompleted()
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(2.5.dp, BauhausBlack),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BauhausBlack,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "RELEASE RESTRICTION & RESUME",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onLeaveAppClick,
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(2.dp, BauhausBlack),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BauhausCanvas,
                    contentColor = BauhausBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "EXIT TO HOME SCREEN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

        } else {
            // --- ACTIVE QUESTION SCREEN ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(3.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Question Count & Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${session.answeredQuestions.size + 1} OF 2",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausBlack,
                                letterSpacing = 1.sp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .background(BauhausBlack)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${activeQuestion.category.displayName.uppercase()} • ${activeQuestion.difficulty.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Text
                    Text(
                        text = activeQuestion.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausBlack,
                            lineHeight = 22.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4 Distinct Options
                    shuffledOptions.forEachIndexed { index, option ->
                        val optionLetter = ('A' + index).toString()
                        val isSelected = selectedOption == option

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .padding(vertical = 4.dp)
                                .border(
                                    BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, BauhausBlack),
                                    RoundedCornerShape(0.dp)
                                )
                                .background(if (isSelected) BauhausYellow else BauhausCanvas)
                                .clickable(
                                    role = Role.RadioButton,
                                    enabled = !isSubmitting
                                ) {
                                    selectedOption = option
                                    feedbackMessage = null
                                    isIncorrectAnswer = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(if (isSelected) BauhausBlack else BauhausMuted)
                                        .border(BorderStroke(1.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetter,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.White else BauhausBlack
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = BauhausBlack
                                    )
                                )
                            }
                        }
                    }

                    // Incorrect Answer Feedback Box
                    if (isIncorrectAnswer && feedbackMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausMuted)
                                .border(BorderStroke(2.dp, BauhausRed), RoundedCornerShape(0.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "INCORRECT ANSWER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausRed
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = feedbackMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(color = BauhausBlack)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Review the explanation above and select another option to proceed.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BauhausBlack
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Answer Button (min 48dp)
                    Button(
                        onClick = {
                            val chosen = selectedOption ?: return@Button
                            if (isSubmitting) return@Button
                            isSubmitting = true

                            val result = viewModel.submitLearningAnswer(
                                packageName = packageName,
                                questionId = activeQuestion.id,
                                selectedOption = chosen
                            )

                            // Refresh session state
                            session = viewModel.getOrInitLearningSession(packageName)

                            if (result.isCorrect) {
                                feedbackMessage = null
                                isIncorrectAnswer = false
                                selectedOption = null
                            } else {
                                isIncorrectAnswer = true
                                feedbackMessage = result.explanation
                            }
                            isSubmitting = false
                        },
                        enabled = selectedOption != null && !isSubmitting,
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(2.5.dp, BauhausBlack),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BauhausBlack,
                            contentColor = Color.White,
                            disabledContainerColor = BauhausMuted,
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (isSubmitting) "VALIDATING..." else "SUBMIT ANSWER",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Escape Button to Home
            Button(
                onClick = onLeaveAppClick,
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(2.dp, BauhausBlack),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BauhausCanvas,
                    contentColor = BauhausBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = "STEP AWAY (EXIT TO HOME)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

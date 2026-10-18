package com.example.doomscroller.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationGuidanceScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("INSTAGRAM", "YOUTUBE")

    val supportedInterests = listOf(
        "Programming & Software Eng.",
        "DSA & Interview Prep",
        "AI & Machine Learning",
        "Science & Mathematics",
        "Technology & Engineering",
        "Education & Self-Improvement",
        "Gaming",
        "Music & Arts",
        "Productivity & Focus"
    )

    val selectedInterests = remember {
        mutableStateListOf(
            "Programming & Software Eng.",
            "DSA & Interview Prep",
            "AI & Machine Learning"
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Stark Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BauhausBlack)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "FEED RECOMMENDATION GUIDANCE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(BauhausYellow)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Honest Policy Transparency Disclaimer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BauhausMuted),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "POLICY & TRANSPARENCY NOTICE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = BauhausRed,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Doomscroll does not scrape, inject code, or claim direct control over third-party feeds. The procedures below utilize officially supported in-app settings to help you reset or train recommendation algorithms toward intentional topics.",
                    style = MaterialTheme.typography.bodySmall.copy(color = BauhausBlack, lineHeight = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selectable Intentional Focus Areas
        Text(
            text = "YOUR INTENDED FOCUS TOPICS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                color = BauhausBlack,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "Select the subjects you want your digital feeds to prioritize:",
            style = MaterialTheme.typography.bodySmall.copy(color = BauhausBlack)
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            supportedInterests.forEach { interest ->
                val isSelected = selectedInterests.contains(interest)
                Box(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .border(
                            BorderStroke(if (isSelected) 2.dp else 1.dp, BauhausBlack),
                            RoundedCornerShape(0.dp)
                        )
                        .background(if (isSelected) BauhausYellow else BauhausCanvas)
                        .clickable {
                            if (isSelected) {
                                selectedInterests.remove(interest)
                            } else {
                                selectedInterests.add(interest)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (if (isSelected) "✓ " else "+ ") + interest,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = BauhausBlack
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Platform Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BauhausCanvas,
            contentColor = BauhausBlack,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 4.dp,
                    color = BauhausBlack
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(BauhausBlack)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.heightIn(min = 48.dp),
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected platform
        if (selectedTab == 0) {
            // Instagram Official Controls
            Column {
                GuidanceStepCard(
                    stepNumber = "01",
                    title = "RESET EXPLORE & REELS ALGORITHM",
                    instructions = "Instagram officially provides an algorithm reset tool:\n1. Open Instagram > Go to your Profile.\n2. Tap Menu (☰) > 'Settings and activity'.\n3. Under 'What you see', tap 'Suggested content'.\n4. Select 'Reset suggested content' and confirm.\nThis erases previously learned video consumption habits and resets recommendations to baseline.",
                    accentColor = BauhausRed
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStepCard(
                    stepNumber = "02",
                    title = "SWITCH TO CHRONOLOGICAL 'FOLLOWING' FEED",
                    instructions = "Avoid the infinite algorithmic Explore feed:\n1. On the Instagram Home tab, tap the top-left 'Instagram' logo.\n2. Tap 'Following' or 'Favorites'.\nThis loads a strict chronological feed containing only accounts you chose to follow, with zero algorithmic suggested posts.",
                    accentColor = BauhausBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStepCard(
                    stepNumber = "03",
                    title = "TRAIN FEED ON INTENTIONAL TOPICS",
                    instructions = "Actively search and interact with your focus topics (${selectedInterests.take(2).joinToString(", ")}):\n1. Search for educational creators in your focus areas.\n2. When an irrelevant Reel appears, tap the three dots (⋯) > tap 'Not interested'.\n3. In 'Suggested content', set 'Sensitive content' to 'Less'.",
                    accentColor = BauhausYellow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        try {
                            val intent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
                            if (intent != null) {
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(2.dp, BauhausBlack),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BauhausBlack,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "OPEN INSTAGRAM APP SETTINGS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        } else {
            // YouTube Official Controls
            Column {
                GuidanceStepCard(
                    stepNumber = "01",
                    title = "PAUSE OR CLEAR WATCH HISTORY",
                    instructions = "YouTube's recommendations are driven entirely by your Watch History:\n1. Open YouTube > Tap your profile icon > Settings.\n2. Tap 'Manage all history'.\n3. Tap 'Controls' > check 'Turn off' (pause watch history), or tap 'Delete' > 'Delete all time'.\nWhen Watch History is paused or empty, YouTube displays a clean search-first screen with zero recommended clickbait.",
                    accentColor = BauhausRed
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStepCard(
                    stepNumber = "02",
                    title = "USE 'NOT INTERESTED' FEEDBACK",
                    instructions = "To purge viral shorts and sensational content:\n1. On any unwanted video card or Short, tap the three dots (⋮).\n2. Tap 'Not interested' or 'Don't recommend channel'.\n3. Within 10-15 ratings, YouTube stops suggesting that entire topic cluster.",
                    accentColor = BauhausBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuidanceStepCard(
                    stepNumber = "03",
                    title = "RESTRICT BROWSE TO 'SUBSCRIPTIONS' TAB",
                    instructions = "Never browse the default 'Home' tab:\n1. Keep only educational, high-signal channels subscribed.\n2. Always open the 'Subscriptions' tab directly when opening YouTube.\n3. Turn off Autoplay in Settings > Autoplay to prevent algorithmic auto-progression.",
                    accentColor = BauhausYellow
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myactivity.google.com/product/youtube"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(2.dp, BauhausBlack),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BauhausBlack,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = "MANAGE YOUTUBE HISTORY CONTROLS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Return Button
        Button(
            onClick = onBackClick,
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
                text = "RETURN TO DASHBOARD",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
private fun GuidanceStepCard(
    stepNumber: String,
    title: String,
    instructions: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
        colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor)
                        .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = BauhausBlack,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = instructions,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BauhausBlack,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

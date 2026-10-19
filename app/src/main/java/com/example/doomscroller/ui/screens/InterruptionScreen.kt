package com.example.doomscroller.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.doomscroller.data.PreferencesManager
import com.example.doomscroller.domain.EnforcementManager
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow
import kotlinx.coroutines.delay

@Composable
fun InterruptionScreen(
    packageName: String,
    onLeaveAppClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back gesture so user does not accidentally return to the distracting app
    BackHandler {
        onLeaveAppClick()
    }

    val context = LocalContext.current
    val enforcementManager = remember { EnforcementManager.getInstance(context) }
    val prefs = remember { PreferencesManager(context) }
    val session = remember(packageName) { prefs.getReminderSession(packageName) }
    val rule = remember(packageName) { prefs.getRules().find { it.packageName == packageName } }
    val ignoredLimit = remember { prefs.getIgnoredReminderLimit() }
    val intervalMinutes = rule?.limitMinutes ?: 5
    val appName = remember(packageName) {
        try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val restrictionDurationMinutes = remember { prefs.getRestrictionDurationMinutes() }
    val pendingIntervention = prefs.getPendingIntervention(packageName)
    val isPendingIntervention = pendingIntervention != null && !pendingIntervention.isAcknowledged && !pendingIntervention.isTimedOut
    val ackRemainingMillis = if (isPendingIntervention) {
        (pendingIntervention!!.deadlineMillis - currentTime).coerceAtLeast(0L)
    } else {
        0L
    }
    val ackRemainingSeconds = (ackRemainingMillis / 1000).toInt()

    // Automatic transition to NO_ACK when 60s acknowledgment deadline expires
    LaunchedEffect(isPendingIntervention, ackRemainingMillis) {
        if (isPendingIntervention && ackRemainingMillis <= 0L) {
            enforcementManager.triggerNoAckTimeout(packageName, pendingIntervention?.eventId)
        }
    }

    val activePenalty = enforcementManager.getActivePenalty(packageName) ?: enforcementManager.activePenalty.value
    val isPenaltyActive = activePenalty != null && !activePenalty.isExpired && (activePenalty.packageName == packageName || packageName.isEmpty() || packageName == "unknown")
    val remainingMillis = if (isPenaltyActive) {
        (activePenalty!!.endTimeMillis - currentTime).coerceAtLeast(0L)
    } else {
        0L
    }
    val remainingSeconds = (remainingMillis / 1000).toInt()
    val countdownMinutes = remainingSeconds / 60
    val countdownSeconds = remainingSeconds % 60
    val countdownFormatted = String.format("%02d:%02d", countdownMinutes, countdownSeconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bauhaus Warning Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(3.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column {
                // Stark Red Header Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BauhausRed)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ENFORCEMENT NOTICE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(BauhausYellow)
                    )
                }

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPenaltyActive) "PENALTY COOLDOWN ACTIVE" else "DAILY LIMIT REACHED",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausBlack,
                            letterSpacing = 1.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .background(BauhausBlack)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = appName.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausYellow,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPendingIntervention) {
                            "60-second acknowledgment window active. Acknowledge now or a $restrictionDurationMinutes-minute penalty restriction will activate automatically."
                        } else {
                            "Usage limit exceeded after sustained foreground activity without acknowledgement. $restrictionDurationMinutes-minute penalty cooldown activated."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = BauhausBlack
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Escalation Metrics Summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            .background(BauhausMuted)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "IGNORED ALERTS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${session.ignoredCount.coerceAtLeast(1)} / $ignoredLimit",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausRed
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INTERVAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${intervalMinutes}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlack
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QUALIFYING USE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${session.qualifyingUsageMillis / 60000}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlack
                                )
                            )
                        }
                    }

                    if (isPendingIntervention) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausMuted)
                                .border(BorderStroke(2.dp, BauhausRed), RoundedCornerShape(0.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AUTOMATIC RESTRICTION IN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausRed
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${ackRemainingSeconds}s",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausRed
                                    )
                                )
                            }
                        }
                    } else if (isPenaltyActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausMuted)
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TIME REMAINING IN COOLDOWN",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = countdownFormatted,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausRed
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Explicit Action Navigation Buttons
        Button(
            onClick = onLeaveAppClick,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(2.5.dp, BauhausBlack),
            colors = ButtonDefaults.buttonColors(
                containerColor = BauhausBlack,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "EXIT TO HOME SCREEN",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (isPendingIntervention) {
                    enforcementManager.acknowledgeIntervention(packageName, pendingIntervention?.eventId)
                } else if (isPenaltyActive) {
                    enforcementManager.acknowledgePenalty(packageName)
                }
                onContinueClick()
            },
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(2.dp, BauhausBlack),
            colors = ButtonDefaults.buttonColors(
                containerColor = BauhausCanvas,
                contentColor = BauhausBlack
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isPendingIntervention) {
                    "ACKNOWLEDGE & CONTINUE"
                } else if (isPenaltyActive) {
                    "ACKNOWLEDGE & GO TO DASHBOARD"
                } else {
                    "RETURN TO DASHBOARD"
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

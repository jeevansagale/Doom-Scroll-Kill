package com.example.doomscroller.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doomscroller.ui.AppViewModel
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySettingsScreen(
    viewModel: AppViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val reminderInterval by viewModel.reminderIntervalMinutes.collectAsState()
    val ignoredLimit by viewModel.ignoredReminderLimit.collectAsState()
    val reminderCap by viewModel.reminderCapPer15Min.collectAsState()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = BauhausCanvas,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BauhausCanvas,
                        titleContentColor = BauhausBlack
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(BauhausBlack)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM CONFIGURATION",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            )
                        }
                    }
                )
                HorizontalDivider(thickness = 2.dp, color = BauhausBlack)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Section 1: Exact Reminder Policy Configuration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(BauhausRed))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REMINDER POLICY CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1A: Reminder Interval Setting
                    Text(
                        text = "REPEAT REMINDER INTERVAL",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "How often repeat reminders appear while a violation condition persists.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .background(BauhausMuted)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$reminderInterval MINUTES",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlack
                                )
                            )
                        }

                        Row {
                            Button(
                                onClick = { viewModel.setReminderIntervalMinutes(reminderInterval - 1) },
                                enabled = reminderInterval > 1,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("-1m", fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.setReminderIntervalMinutes(reminderInterval + 1) },
                                enabled = reminderInterval < 60,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("+1m", fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = reminderInterval.toFloat(),
                        onValueChange = { viewModel.setReminderIntervalMinutes(it.toInt()) },
                        valueRange = 1f..60f,
                        steps = 58,
                        colors = SliderDefaults.colors(
                            thumbColor = BauhausBlack,
                            activeTrackColor = BauhausBlack,
                            inactiveTrackColor = BauhausMuted
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MIN: 1m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("DEFAULT: 5m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("MAX: 60m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(thickness = 1.dp, color = BauhausBlack.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1B: Parameter Z: Ignored Reminder Limit Setting
                    Text(
                        text = "PARAMETER Z: IGNORED REMINDER LIMIT",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Consecutive reminders that may be ignored before triggering 5-minute cooldown enforcement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .background(BauhausRed)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$ignoredLimit REMINDERS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }

                        Row {
                            Button(
                                onClick = { viewModel.setIgnoredReminderLimit(ignoredLimit - 1) },
                                enabled = ignoredLimit > 1,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("-", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.setIgnoredReminderLimit(ignoredLimit + 1) },
                                enabled = ignoredLimit < 10,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("+", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MIN: 1 REMINDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("DEFAULT: 3 REMINDERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("MAX: 10 REMINDERS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(thickness = 1.dp, color = BauhausBlack.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1C: Reminder Frequency Cap Setting
                    Text(
                        text = "MAXIMUM REMINDER FREQUENCY",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rolling 15-minute upper-bound limit. Reminders beyond this cap are strictly suppressed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .background(BauhausYellow)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$reminderCap ALERTS / 15 MIN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlack
                                )
                            )
                        }

                        Row {
                            Button(
                                onClick = { viewModel.setReminderCapPer15Min(reminderCap - 1) },
                                enabled = reminderCap > 1,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("-", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { viewModel.setReminderCapPer15Min(reminderCap + 1) },
                                enabled = reminderCap < 6,
                                shape = RoundedCornerShape(0.dp),
                                border = BorderStroke(1.5.dp, BauhausBlack),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BauhausCanvas,
                                    contentColor = BauhausBlack,
                                    disabledContainerColor = BauhausMuted
                                )
                            ) {
                                Text("+", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MIN: 1 / 15m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("DEFAULT: 3 / 15m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("MAX: 6 / 15m", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BauhausMuted)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "NOTE: The 5-minute penalty cooldown is separate from the reminder interval. At Z ignored reminders, enforcement activates exactly once.",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BauhausBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1.5: System Alert Window (Overlay Permission)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if (hasOverlayPermission) BauhausBlue else BauhausYellow))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FULL-SCREEN OVERLAY ENFORCEMENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (hasOverlayPermission) "OVERLAY PERMISSION ACTIVE" else "HEADS-UP ONLY (OVERLAY PERMISSION NOT GRANTED)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (hasOverlayPermission) BauhausBlue else BauhausRed
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (hasOverlayPermission) {
                            "Warning screens launch directly on top of monitored apps when escalation limit is reached."
                        } else {
                            "Due to Android restrictions, without 'Display over other apps' permission, warnings appear as high-priority heads-up notifications with action buttons."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )

                    if (!hasOverlayPermission) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(2.dp, BauhausBlack),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BauhausBlack,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "GRANT OVERLAY PERMISSION",
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Daily reset parameters
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausMuted),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ENFORCEMENT SCHEDULE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DAILY QUOTA RESET: 00:00 MIDNIGHT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BauhausBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Usage statistics calculate daily accumulations reset each calendar day at midnight.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Privacy Manifest
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(BauhausBlue))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DATA AUTONOMY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ZERO NETWORK TELEMETRY",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BauhausBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All app usage queries, evaluation calculations, and penalty states execute locally on-device. No telemetry is collected or transmitted.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BauhausBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: Architecture
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausMuted),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(BauhausYellow))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARCHITECTURE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "DOOMSCROLLER ENGINE V1.3",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BauhausBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bounded reminder rate limiting, 15-minute rolling window enforcement, and adaptive power-state throttling.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BauhausBlack.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

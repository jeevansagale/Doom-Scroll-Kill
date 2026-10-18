package com.example.doomscroller.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@Composable
fun PermissionSetupScreen(
    hasUsageAccess: Boolean = false,
    hasNotificationPermission: Boolean = false,
    onUsageAccessClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onRefreshPermissions: () -> Unit = {},
    onContinueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recheck system authorization state dynamically every time the screen resumes (e.g. returning from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onNotificationClick()
        onRefreshPermissions()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(BauhausBlack)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SYSTEM AUTHORIZATIONS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = BauhausBlack
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Permission 1: Usage Access (MANDATORY)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = if (hasUsageAccess) BauhausCanvas else BauhausMuted),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if (hasUsageAccess) BauhausBlue else BauhausRed))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. USAGE STATS ACCESS",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(if (hasUsageAccess) BauhausBlue else BauhausRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasUsageAccess) "GRANTED" else "MANDATORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasUsageAccess) {
                        "✓ Active authorization confirmed. Doomscroller is authorized to monitor foreground app usage and evaluate daily limits."
                    } else {
                        "System authorization required to measure on-screen durations and enforce quotas. Doomscroller cannot function without this authorization."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = BauhausBlack
                )

                Spacer(modifier = Modifier.height(14.dp))
                if (hasUsageAccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            .background(BauhausMuted)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ USAGE ACCESS AUTHORIZED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausBlack,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            onUsageAccessClick()
                        },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(2.dp, BauhausBlack),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BauhausYellow,
                            contentColor = BauhausBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "AUTHORIZE USAGE ACCESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permission 2: Notifications (OPTIONAL)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = if (hasNotificationPermission) BauhausCanvas else BauhausMuted),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if (hasNotificationPermission) BauhausBlue else BauhausYellow))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. NOTIFICATION PRIVILEGE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(if (hasNotificationPermission) BauhausBlue else BauhausBlack)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (hasNotificationPermission) "GRANTED" else "OPTIONAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasNotificationPermission) {
                        "✓ Active notification privilege confirmed. Warning banners and cooldown countdowns will be delivered."
                    } else {
                        "Optional privilege used to deliver heads-up warning banners and cooldown notifications. You may skip this if preferred."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = BauhausBlack
                )

                Spacer(modifier = Modifier.height(14.dp))
                if (hasNotificationPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            .background(BauhausMuted)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ NOTIFICATIONS AUTHORIZED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausBlack,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                                onNotificationClick()
                            }
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
                            text = "ENABLE NOTIFICATIONS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Proceed Button (Guarded by mandatory Usage Access)
        Button(
            onClick = onContinueClick,
            enabled = hasUsageAccess,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(2.5.dp, if (hasUsageAccess) BauhausBlack else BauhausMuted),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasUsageAccess) BauhausBlack else BauhausMuted,
                contentColor = if (hasUsageAccess) Color.White else BauhausBlack.copy(alpha = 0.5f),
                disabledContainerColor = BauhausMuted,
                disabledContentColor = BauhausBlack.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (hasUsageAccess) "PROCEED TO DASHBOARD" else "USAGE ACCESS REQUIRED",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        if (!hasUsageAccess) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grant Usage Stats Access above to unlock the dashboard and enable monitoring.",
                style = MaterialTheme.typography.labelSmall,
                color = BauhausRed,
                textAlign = TextAlign.Center
            )
        }
    }
}

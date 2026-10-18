package com.example.doomscroller.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.doomscroller.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.doomscroller.data.AppRule
import com.example.doomscroller.ui.AppViewModel
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onAddAppClick: () -> Unit,
    onAppClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onRecommendationGuidanceClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    val hasUsageAccess by viewModel.hasUsageAccess.collectAsState()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsState()
    val todayUsage by viewModel.todayUsage.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val activePenalty by viewModel.activePenalty.collectAsState()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.checkPermissions() // Also refreshes usage
        }
    }
    
    val totalUsedMillis = rules.sumOf { rule -> todayUsage[rule.packageName] ?: 0L }
    val totalUsedMinutes = (totalUsedMillis / 1000 / 60).toInt()
    val appNamesByPackage = remember(installedApps) { installedApps.associate { it.packageName to it.name } }

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
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "DoomScroller Logo",
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "DOOMSCROLLER",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = onSettingsClick,
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(2.dp, BauhausBlack),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BauhausCanvas,
                                contentColor = BauhausBlack
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "SETTINGS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                )
                HorizontalDivider(thickness = 2.dp, color = BauhausBlack)
            }
        },
        floatingActionButton = {
            Button(
                onClick = onAddAppClick,
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(2.dp, BauhausBlack),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BauhausRed,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "+ ADD TARGET",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Bauhaus Hero Metric Panel (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(2.5.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                    colors = CardDefaults.cardColors(containerColor = BauhausMuted),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column {
                        // Geometric Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BauhausBlack)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SCREEN TIME",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = BauhausYellow,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BauhausYellow, CircleShape)
                            )
                        }

                        // Numeric Content
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${totalUsedMinutes / 60}H ${totalUsedMinutes % 60}M",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BauhausBlack
                                    )
                                )
                                Text(
                                    text = "ACCUMULATED ACROSS MONITORED TARGETS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BauhausBlack.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BauhausBlue)
                            )
                        }
                    }
                }
            }

            // Active Penalty Alert Banner (Full width)
            if (activePenalty != null && !activePenalty!!.isExpired) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                        colors = CardDefaults.cardColors(containerColor = BauhausRed),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(BauhausYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "!",
                                    color = BauhausBlack,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "ACTIVE ENFORCEMENT COOLDOWN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                                val penalizedName = appNamesByPackage[activePenalty!!.packageName] ?: activePenalty!!.packageName
                                val remainingSec = (activePenalty!!.remainingMillis / 1000).coerceAtLeast(0)
                                Text(
                                    text = "$penalizedName: ${remainingSec / 60}m ${remainingSec % 60}s remaining",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Permissions Alert Banner (Full width)
            if (!hasUsageAccess || !hasNotificationPermission) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                        colors = CardDefaults.cardColors(containerColor = BauhausRed),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "!",
                                    color = BauhausRed,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "PERMISSIONS REQUIRED. MONITORING IS INACTIVE.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Section Header (Full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(BauhausBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MONITORED TARGETS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausBlack
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    HorizontalDivider(
                        thickness = 1.5.dp,
                        color = BauhausBlack,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Empty state (Full width)
            if (rules.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NO APPS MONITORED",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                                color = BauhausBlack
                            )
                            Text(
                                text = "TAP + TO ADD APPS TO QUOTA CONTROL",
                                style = MaterialTheme.typography.labelSmall,
                                color = BauhausBlack.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(
                    items = rules,
                    key = { it.packageName },
                    contentType = { "rule_item" }
                ) { rule ->
                    val usedMillis = todayUsage[rule.packageName] ?: 0L
                    val usedMinutes = (usedMillis / 1000 / 60).toInt()
                    val appName = appNamesByPackage[rule.packageName] ?: rule.packageName
                    AppUsageCard(
                        rule = rule, 
                        appName = appName,
                        usedMinutes = usedMinutes, 
                        onClick = { onAppClick(rule.packageName) }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                        .clickable { onRecommendationGuidanceClick() },
                    colors = CardDefaults.cardColors(containerColor = BauhausMuted),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ALGORITHM CURATION GUIDANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BauhausBlue,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Learn how to reset or curate YouTube and Instagram recommendations toward intentional focus topics.",
                                style = MaterialTheme.typography.bodySmall.copy(color = BauhausBlack)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(BauhausYellow)
                                .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(80.dp)) // fab padding
            }
        }
    }
}

@Composable
fun AppUsageCard(rule: AppRule, appName: String, usedMinutes: Int, onClick: () -> Unit) {
    val progress = if (rule.limitMinutes > 0) (usedMinutes.toFloat() / rule.limitMinutes).coerceIn(0f, 1f) else 0f
    val isWarning = progress >= 0.9f
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (isWarning) BauhausRed else BauhausBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BauhausBlack
                    )
                }
                Text(
                    text = "${usedMinutes}M / ${rule.limitMinutes}M", 
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                    color = if (isWarning) BauhausRed else BauhausBlack
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bauhaus Geometric Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(BauhausMuted)
                    .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .fillMaxHeight()
                            .background(if (isWarning) BauhausRed else BauhausYellow)
                    )
                }
            }
            
            if (!rule.isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PAUSED", 
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = BauhausRed
                )
            }
        }
    }
}

package com.example.doomscroller.feature.discovery.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doomscroller.feature.discovery.domain.ContentItem
import com.example.doomscroller.feature.discovery.domain.ContentPlatform
import com.example.doomscroller.feature.discovery.ui.components.ContentItemCard
import com.example.doomscroller.feature.discovery.ui.components.IntentionModal
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@Suppress("DEPRECATION")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoveryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscoveryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Bauhaus Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BauhausBlack)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onBackClick() }
                            .padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "← BACK",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausYellow,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Text(
                        text = "TOPIC DISCOVERY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(BauhausRed))
                    Box(modifier = Modifier.size(10.dp).background(BauhausBlue))
                    Box(modifier = Modifier.size(10.dp).background(BauhausYellow))
                }
            }

            // Tabs: DISCOVER vs SAVED
            TabRow(
                selectedTabIndex = uiState.activeTab,
                containerColor = BauhausCanvas,
                contentColor = BauhausBlack,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab]),
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
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.onTabSelect(0) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    text = {
                        Text(
                            text = "DISCOVER TOPICS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.onTabSelect(1) },
                    modifier = Modifier.heightIn(min = 48.dp),
                    text = {
                        Text(
                            text = "SAVED (${uiState.savedItems.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                )
            }

            // Adaptive Content Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.activeTab == 0) {
                    // Search & Filter Header Section (Spans all columns)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            // Search input
                            OutlinedTextField(
                                value = uiState.query,
                                onValueChange = { viewModel.onQueryChange(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                                placeholder = {
                                    Text(
                                        text = "Search educational topic (e.g., System Design)...",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = BauhausBlack.copy(alpha = 0.5f))
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(0.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = BauhausBlack,
                                    unfocusedBorderColor = BauhausBlack,
                                    focusedTextColor = BauhausBlack,
                                    unfocusedTextColor = BauhausBlack
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Suggested Topic Pills
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                uiState.suggestedTopics.forEach { topic ->
                                    val isSelected = uiState.query.equals(topic, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .border(
                                                BorderStroke(if (isSelected) 2.dp else 1.dp, BauhausBlack),
                                                RoundedCornerShape(0.dp)
                                            )
                                            .background(if (isSelected) BauhausYellow else BauhausCanvas)
                                            .clickable { viewModel.onTopicPillClick(topic) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = topic,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                color = BauhausBlack
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Platform Filter Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ContentPlatform.values().forEach { platform ->
                                    val isSelected = uiState.selectedPlatform == platform
                                    val chipBg = when {
                                        !isSelected -> BauhausCanvas
                                        platform == ContentPlatform.YOUTUBE -> BauhausRed
                                        platform == ContentPlatform.INSTAGRAM -> BauhausBlue
                                        else -> BauhausBlack
                                    }
                                    val textColor = if (isSelected) Color.White else BauhausBlack

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .border(
                                                BorderStroke(if (isSelected) 2.dp else 1.dp, BauhausBlack),
                                                RoundedCornerShape(0.dp)
                                            )
                                            .background(chipBg)
                                            .clickable { viewModel.onPlatformSelect(platform) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = platform.displayName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                color = textColor,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Source notice / Policy transparency banner
                            if (!uiState.sourceNotice.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(BorderStroke(1.dp, BauhausBlack))
                                        .background(BauhausMuted)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "ℹ " + uiState.sourceNotice!!,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = BauhausBlack,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Loading State
                    if (uiState.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = BauhausBlack)
                            }
                        }
                    } else if (uiState.results.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(2.dp, BauhausBlack))
                                    .background(Color.White)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No intentional content found for \"${uiState.query}\". Try another educational topic.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BauhausBlack
                                    )
                                )
                            }
                        }
                    } else {
                        // Render Content Cards
                        items(uiState.results, key = { it.id }) { item ->
                            ContentItemCard(
                                item = item,
                                onOpenClick = { viewModel.onOpenItemClick(it) },
                                onToggleSaveClick = { viewModel.onToggleSave(it) }
                            )
                        }
                    }
                } else {
                    // SAVED TAB
                    if (uiState.savedItems.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(2.dp, BauhausBlack))
                                    .background(Color.White)
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "NO SAVED ITEMS YET",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = BauhausBlack
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Bookmark videos and creators from the Discover tab for intentional study.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = BauhausBlack)
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.savedItems, key = { it.id }) { item ->
                            ContentItemCard(
                                item = item,
                                onOpenClick = { viewModel.onOpenItemClick(it) },
                                onToggleSaveClick = { viewModel.onToggleSave(it) }
                            )
                        }
                    }
                }
            }
        }

        // Intention Modal Dialog
        uiState.selectedItemForIntention?.let { item ->
            IntentionModal(
                item = item,
                onConfirm = { minutes ->
                    viewModel.onDismissIntention()
                    openContentIntent(context, item, minutes)
                },
                onDismiss = { viewModel.onDismissIntention() }
            )
        }
    }
}

private fun openContentIntent(context: Context, item: ContentItem, minutes: Int) {
    Toast.makeText(
        context,
        "Session commitment: $minutes min. Focus intentionally!",
        Toast.LENGTH_SHORT
    ).show()

    val parsedUri = Uri.parse(item.url)
    val intent = Intent(Intent.ACTION_VIEW, parsedUri).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this link.", Toast.LENGTH_SHORT).show()
    }
}

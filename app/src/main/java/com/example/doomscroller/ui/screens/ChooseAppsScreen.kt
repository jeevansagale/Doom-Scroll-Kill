package com.example.doomscroller.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.doomscroller.ui.AppViewModel
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAppsScreen(
    viewModel: AppViewModel,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val rules by viewModel.rules.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    val selectedPackages = remember(rules) { rules.map { it.packageName }.toSet() }

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
                                    .background(BauhausBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SELECT TARGETS",
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
        if (installedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BauhausBlack, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CATALOGING APPLICATIONS...",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = installedApps,
                    key = { it.packageName },
                    contentType = { "app_item" }
                ) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            .background(if (isSelected) BauhausMuted else BauhausCanvas)
                            .clickable { onAppSelected(app.packageName) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isSelected) BauhausBlack else Color.Transparent)
                                    .border(BorderStroke(1.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = app.name.uppercase(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                ),
                                color = BauhausBlack
                            )
                        }
                        
                        // Geometric square indicator
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .background(if (isSelected) BauhausBlack else BauhausCanvas),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(BauhausYellow)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

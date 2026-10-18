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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseAppsScreen(
    viewModel: AppViewModel,
    onAppSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val rules by viewModel.rules.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    val selectedPackages = remember(rules) { rules.map { it.packageName }.toSet() }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
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

                // Responsive Search Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        placeholder = {
                            Text(
                                text = "SEARCH APPLICATIONS...",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = BauhausBlack.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BauhausBlack,
                            unfocusedBorderColor = BauhausBlack,
                            focusedContainerColor = BauhausCanvas,
                            unfocusedContainerColor = BauhausCanvas,
                            focusedTextColor = BauhausBlack,
                            unfocusedTextColor = BauhausBlack
                        ),
                        shape = RoundedCornerShape(0.dp)
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = BauhausBlack.copy(alpha = 0.3f))
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
        } else if (filteredApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
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
                        text = "NO MATCHING APPLICATIONS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = BauhausBlack
                    )
                    Text(
                        text = "TRY A DIFFERENT SEARCH TERM",
                        style = MaterialTheme.typography.labelSmall,
                        color = BauhausBlack.copy(alpha = 0.6f)
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
                    items = filteredApps,
                    key = { it.packageName },
                    contentType = { "app_item" }
                ) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    val appBitmap = remember(app.icon) {
                        app.icon?.let { d ->
                            try {
                                d.toBitmap(96, 96).asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(if (isSelected) 2.dp else 1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                            .background(if (isSelected) BauhausMuted else BauhausCanvas)
                            .clickable { onAppSelected(app.packageName) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (appBitmap != null) {
                                Image(
                                    bitmap = appBitmap,
                                    contentDescription = app.name,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .border(BorderStroke(1.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isSelected) BauhausBlack else BauhausCanvas)
                                        .border(BorderStroke(1.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) Color.White else BauhausBlack
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = app.name.uppercase(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                    ),
                                    color = BauhausBlack,
                                    maxLines = 1
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = BauhausBlack.copy(alpha = 0.6f)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Geometric square indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                                .background(if (isSelected) BauhausBlack else BauhausCanvas),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
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

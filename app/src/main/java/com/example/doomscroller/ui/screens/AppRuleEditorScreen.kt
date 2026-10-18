package com.example.doomscroller.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AppRuleEditorScreen(
    packageName: String,
    viewModel: AppViewModel,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    val existingRule = rules.find { it.packageName == packageName }

    var limitMinutes by remember { mutableFloatStateOf(existingRule?.limitMinutes?.toFloat() ?: 60f) }
    var isEnabled by remember { mutableStateOf(existingRule?.isEnabled ?: true) }
    
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
                                    .background(BauhausYellow)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RULE PARAMETERS",
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
                .padding(20.dp)
        ) {
            // Target App Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausMuted),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONITORED TARGET",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BauhausBlack
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Enabled toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp))
                    .background(BauhausCanvas)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ENFORCEMENT ACTIVE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = if (isEnabled) "QUOTA WILL BE MEASURED & ENFORCED" else "MONITORING SUSPENDED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) BauhausBlue else BauhausRed
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BauhausBlack,
                        uncheckedThumbColor = BauhausBlack,
                        uncheckedTrackColor = BauhausMuted
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Limit Setting Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(2.dp, BauhausBlack), RoundedCornerShape(0.dp)),
                colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DAILY QUOTA LIMIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${limitMinutes.toInt()}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = BauhausBlack
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MINUTES / DAY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BauhausBlue
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Slider(
                        value = limitMinutes,
                        onValueChange = { limitMinutes = it },
                        valueRange = 1f..300f,
                        steps = 298,
                        colors = SliderDefaults.colors(
                            thumbColor = BauhausRed,
                            activeTrackColor = BauhausBlack,
                            inactiveTrackColor = BauhausMuted
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Static Penalty rule details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.5.dp, BauhausBlack), RoundedCornerShape(0.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PENALTY COOLDOWN: 5 MIN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "MAX WARNINGS: 3",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (existingRule != null) {
                Button(
                    onClick = {
                        viewModel.removeRule(packageName)
                        onSaveClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(2.dp, BauhausRed),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BauhausCanvas,
                        contentColor = BauhausRed
                    )
                ) {
                    Text(
                        text = "DELETE RULE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Button(
                onClick = {
                    viewModel.saveRule(AppRule(packageName, limitMinutes.toInt(), isEnabled))
                    onSaveClick()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(2.5.dp, BauhausBlack),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BauhausBlack,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "SAVE RULE",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doomscroller.ui.theme.BauhausBlack
import com.example.doomscroller.ui.theme.BauhausBlue
import com.example.doomscroller.ui.theme.BauhausCanvas
import com.example.doomscroller.ui.theme.BauhausMuted
import com.example.doomscroller.ui.theme.BauhausRed
import com.example.doomscroller.ui.theme.BauhausYellow

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BauhausCanvas)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bauhaus Manifest Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(3.dp, BauhausBlack), RoundedCornerShape(0.dp)),
            colors = CardDefaults.cardColors(containerColor = BauhausCanvas),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column {
                // Bauhaus Primary Color Trio Bar
                Row(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                    Box(modifier = Modifier.weight(1f).background(BauhausRed))
                    Box(modifier = Modifier.weight(1f).background(BauhausYellow))
                    Box(modifier = Modifier.weight(1f).background(BauhausBlue))
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Geometric Header Symbols
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(16.dp).background(BauhausBlack))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(16.dp).background(BauhausYellow, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(16.dp).background(BauhausBlue))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "DOOMSCROLLER",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp,
                            color = BauhausBlack
                        )
                    )
                    Text(
                        text = "DISCIPLINE THROUGH DESIGN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = BauhausRed,
                            letterSpacing = 1.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "We monitor designated applications to enforce daily screen time limits. " +
                                "All metrics remain strictly on-device. When your quota expires, cooldown enforcement begins.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 22.sp,
                            color = BauhausBlack
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinueClick,
            shape = RoundedCornerShape(0.dp),
            border = BorderStroke(2.5.dp, BauhausBlack),
            colors = ButtonDefaults.buttonColors(
                containerColor = BauhausBlack,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "BEGIN SETUP",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

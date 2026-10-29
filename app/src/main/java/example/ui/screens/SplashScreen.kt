package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.meypod.al_azan.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle animated progress
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Auto-advance after 2.5 seconds or click
    LaunchedEffect(Unit) {
        delay(2500)
        onNavigateToHome()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SalahDarkBg)
            .clickable(onClick = onNavigateToHome)
            .testTag("splash_screen")
    ) {
        // Night sky background image with moon and mosque silhouette
        Image(
            painter = painterResource(id = R.drawable.mosque),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for smooth contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66081316),
                            Color(0xE6081316)
                        ),
                        startY = 300f
                    )
                )
        )

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo Icon Container with Gold Border
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF0D252B))
                    .border(2.dp, SalahGoldLight, RoundedCornerShape(28.dp))
                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = SalahGold)
                    .testTag("splash_logo"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "SalahGuard Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Golden Highlight
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Salah",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = SalahTextWhite
                    )
                )
                Text(
                    text = "Guard",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = SalahGold
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitles exactly as reference
            Text(
                text = "Stay on Track",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = SalahTextWhite.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stay Closer to Allah",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = SalahTextSecondary.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Progress Bar
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(SalahCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(SalahGoldLight, SalahGold)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap to enter",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SalahTextSecondary.copy(alpha = pulseAlpha),
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

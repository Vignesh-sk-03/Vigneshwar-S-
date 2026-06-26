package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import kotlinx.coroutines.delay

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brush)
    )
}

@Composable
fun LoadingSkeletonList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerItem(modifier = Modifier.width(120.dp).height(20.dp))
                        ShimmerItem(modifier = Modifier.width(60.dp).height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerItem(modifier = Modifier.size(48.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            ShimmerItem(modifier = Modifier.width(100.dp).height(16.dp))
                            ShimmerItem(modifier = Modifier.width(140.dp).height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamLogo(
    teamShort: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null
) {
    val color1 = when (teamShort) {
        "IND" -> Color(0xFF0D47A1)
        "AUS" -> Color(0xFFFFD54F)
        "ENG" -> Color(0xFFC62828)
        "PAK" -> Color(0xFF1B5E20)
        "RSA" -> Color(0xFF00796B)
        "NZ" -> Color(0xFF212121)
        else -> MaterialTheme.colorScheme.secondary
    }
    
    val color2 = when (teamShort) {
        "IND" -> Color(0xFFE65100)
        "AUS" -> Color(0xFF1565C0)
        "ENG" -> Color(0xFF1565C0)
        "PAK" -> Color(0xFFE0F2F1)
        "RSA" -> Color(0xFFFBC02D)
        "NZ" -> Color(0xFF757575)
        else -> MaterialTheme.colorScheme.primary
    }

    val backgroundModifier = if (backgroundColor != null) {
        Modifier.background(backgroundColor)
    } else {
        Modifier.background(Brush.sweepGradient(colors = listOf(color1, color2, color1)))
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(backgroundModifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = teamShort.take(3),
            color = if (teamShort == "AUS" && backgroundColor == null) Color.Black else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TournamentBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    color = CricGreen.copy(alpha = 0.05f),
                    radius = size.maxDimension * 0.4f,
                    center = Offset(size.width * 0.85f, size.height * 0.5f)
                )
            }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CricPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Custom friendly Refresh structure
    Box(modifier = modifier.fillMaxSize()) {
        content()
        
        AnimatedVisibility(
            visible = isRefreshing,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Refreshing Scores...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PushNotificationAlertOverlay(
    notifications: List<String>,
    onClear: () -> Unit
) {
    var visibleNotification by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(notifications) {
        if (notifications.isNotEmpty()) {
            visibleNotification = notifications.first()
            delay(4000) // Display banner for 4 seconds
            visibleNotification = null
        }
    }

    AnimatedVisibility(
        visible = visibleNotification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        visibleNotification?.let { msg ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.fillMaxWidth().clickable { onClear() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CricGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = CricGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CRIC.TAUGHT LIVE ALERT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CricGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

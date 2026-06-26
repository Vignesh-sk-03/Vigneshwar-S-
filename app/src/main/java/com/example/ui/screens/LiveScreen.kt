package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    viewModel: CricketViewModel,
    onNavigateToMatch: (String) -> Unit
) {
    val matchesState by viewModel.matchesState.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    fun refreshData() {
        scope.launch {
            isRefreshing = true
            viewModel.fetchMatches(force = true)
            delay(1000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pulsing LIVE dot
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CricRed.copy(alpha = alpha))
                        )
                        Text(
                            text = "Live Match Hub",
                            fontWeight = FontWeight.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        CricPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = ::refreshData,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val state = matchesState) {
                is Resource.Loading -> {
                    LoadingSkeletonList()
                }
                is Resource.Error -> {
                    ErrorStateView(state.message, ::refreshData)
                }
                is Resource.Success -> {
                    val liveMatches = state.data.filter { it.status == "LIVE" }
                    if (liveMatches.isEmpty()) {
                        EmptyLiveState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(liveMatches) { match ->
                                MatchCard(
                                    match = match,
                                    onClick = { onNavigateToMatch(match.id) },
                                    onFavoriteToggle = {
                                        viewModel.toggleMatchFavorite(match.id, !match.isFavorite)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLiveState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsCricket,
                contentDescription = "No Live Matches",
                tint = CricGreen,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Live Matches Active",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "There are no live fixtures updating right now. Go to Schedules to check upcoming games or trigger the simulation loop inside settings to create live scores!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

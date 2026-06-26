package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.components.TeamLogo
import com.example.ui.components.TournamentBanner
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CricketViewModel,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val matchesState by viewModel.matchesState.collectAsState()
    val pushNotifications by viewModel.pushNotifications.collectAsState()
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
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cric_taught_logo),
                            contentDescription = "Cric.taught Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cric",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = ".taught",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { viewModel.clearNotifications() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Notifications",
                            tint = CricRed
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
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
                    HomeScreenContent(
                        matches = state.data,
                        pushNotifications = pushNotifications,
                        onMatchClick = onNavigateToMatch,
                        onFavoriteToggle = { matchId, isFav ->
                            viewModel.toggleMatchFavorite(matchId, isFav)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    matches: List<Match>,
    pushNotifications: List<String>,
    onMatchClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit
) {
    val liveMatches = matches.filter { it.status == "LIVE" }
    val upcomingMatches = matches.filter { it.status == "UPCOMING" }
    val completedMatches = matches.filter { it.status == "COMPLETED" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tournament Banner Hero
        item {
            TournamentBanner(
                title = "ICC Men's Cricket World Cup 2026",
                subtitle = "Featured Tournament",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Live Alerts Feed (Push simulation display)
        if (pushNotifications.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "SIMULATED PUSH LOGS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CricGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pushNotifications.take(3).forEach { note ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = "•", color = CricGreen, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Match horizontal row
        if (liveMatches.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE MATCHES",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CricRed)
                            )
                            Text(
                                text = "LIVE UPDATES",
                                style = MaterialTheme.typography.labelSmall,
                                color = CricRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(liveMatches) { match ->
                            MatchCard(
                                match = match,
                                onClick = { onMatchClick(match.id) },
                                onFavoriteToggle = { onFavoriteToggle(match.id, !match.isFavorite) },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upcoming section
        if (upcomingMatches.isNotEmpty()) {
            item {
                Text(
                    text = "UPCOMING MATCHES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(upcomingMatches) { match ->
                MatchCard(
                    match = match,
                    onClick = { onMatchClick(match.id) },
                    onFavoriteToggle = { onFavoriteToggle(match.id, !match.isFavorite) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Completed section
        if (completedMatches.isNotEmpty()) {
            item {
                Text(
                    text = "COMPLETED MATCHES",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(completedMatches) { match ->
                MatchCard(
                    match = match,
                    onClick = { onMatchClick(match.id) },
                    onFavoriteToggle = { onFavoriteToggle(match.id, !match.isFavorite) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Tournament + status tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.seriesName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Status badge
                val (badgeColor, textColor, text) = when (match.status) {
                    "LIVE" -> Triple(CricRed.copy(alpha = 0.15f), CricRed, "LIVE")
                    "UPCOMING" -> Triple(CricGold.copy(alpha = 0.15f), CricGold, "UPCOMING")
                    else -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), MaterialTheme.colorScheme.onSurface, "COMPLETED")
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Team A details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeamLogo(match.teamAShort)
                    Column {
                        Text(
                            text = match.teamA,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (match.status == "LIVE" && match.currentInnings == match.teamAShort) {
                            Text(
                                text = "Batting",
                                style = MaterialTheme.typography.labelSmall,
                                color = CricGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = match.teamAScore,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = if (match.currentInnings == match.teamAShort) CricGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Team B details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeamLogo(match.teamBShort)
                    Column {
                        Text(
                            text = match.teamB,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (match.status == "LIVE" && match.currentInnings == match.teamBShort) {
                            Text(
                                text = "Batting",
                                style = MaterialTheme.typography.labelSmall,
                                color = CricGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = match.teamBScore,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = if (match.currentInnings == match.teamBShort) CricGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Match venue/result and Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (match.status == "LIVE") match.lastComment else match.result,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (match.status == "LIVE") CricGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (match.status == "LIVE") FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Venue",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = match.venue,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Match",
                        tint = if (match.isFavorite) CricRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = "Offline",
            tint = CricRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Data Access Error",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry Connection")
        }
    }
}

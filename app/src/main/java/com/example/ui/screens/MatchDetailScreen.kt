package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.Resource
import com.example.ui.components.CricPullToRefreshBox
import com.example.ui.components.LoadingSkeletonList
import com.example.ui.components.TeamLogo
import com.example.ui.theme.CricGreen
import com.example.ui.theme.CricGold
import com.example.ui.theme.CricRed
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    viewModel: CricketViewModel,
    onBack: () -> Unit
) {
    val matchDetailState by viewModel.selectedMatchDetails.collectAsState()
    val scorecardState by viewModel.selectedScorecard.collectAsState()
    val commentaryState by viewModel.selectedCommentary.collectAsState()
    
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Scorecard, 1: Commentary, 2: Info
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(matchId) {
        viewModel.selectMatch(matchId)
    }

    fun refreshMatchData() {
        scope.launch {
            isRefreshing = true
            viewModel.selectMatch(matchId)
            delay(1000)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            val title = when (val res = matchDetailState) {
                is Resource.Success -> "${res.data.teamAShort} vs ${res.data.teamBShort}"
                else -> "Match Details"
            }
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (matchDetailState is Resource.Success) {
                        val match = (matchDetailState as Resource.Success<Match>).data
                        IconButton(onClick = { viewModel.toggleMatchFavorite(match.id, !match.isFavorite) }) {
                            Icon(
                                imageVector = if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (match.isFavorite) CricRed else MaterialTheme.colorScheme.onBackground
                            )
                        }
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
            onRefresh = ::refreshMatchData,
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val state = matchDetailState) {
                is Resource.Loading -> LoadingSkeletonList()
                is Resource.Error -> ErrorStateView(state.message, ::refreshMatchData)
                is Resource.Success -> {
                    val match = state.data
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Hero Live Scorecard Card Header
                        MatchHeroHeader(match)

                        // Scorecard, Commentary, Info tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                Text("SCORECARD", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("COMMENTARY", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                Text("MATCH INFO", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Tab content
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                0 -> ScorecardTabContent(scorecardState)
                                1 -> CommentaryTabContent(commentaryState)
                                2 -> InfoTabContent(match)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchHeroHeader(match: Match) {
    Card(
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = match.seriesName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team A
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                    TeamLogo(match.teamAShort, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = match.teamAShort, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(text = match.teamAScore, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = CricGreen, textAlign = TextAlign.Center)
                    Text(text = "Overs: ${match.teamAOvers}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // VS Badge / Ticker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.status == "LIVE") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CricRed)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "LIVE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    } else {
                        Text(text = "VS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = match.time, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                // Team B
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                    TeamLogo(match.teamBShort, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = match.teamBShort, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(text = match.teamBScore, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = CricGreen, textAlign = TextAlign.Center)
                    Text(text = "Overs: ${match.teamBOvers}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (match.status == "LIVE") match.lastComment else match.result,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (match.status == "LIVE") CricGreen else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun ScorecardTabContent(scorecardState: Resource<Scorecard>) {
    when (scorecardState) {
        is Resource.Loading -> LoadingSkeletonList()
        is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(scorecardState.message) }
        is Resource.Success -> {
            val scorecard = scorecardState.data
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Innings Header
                item {
                    Text(
                        text = scorecard.inningsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Batsman Table Header
                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("BATSMAN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(3f))
                            Text("R", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("B", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("4s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("6s", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("SR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
                        }
                    }
                }

                // Batsmen Scores
                items(scorecard.batsmen) { batsman ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(3f)) {
                            Text(batsman.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(batsman.howOut, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${batsman.runs}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${batsman.balls}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${batsman.fours}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${batsman.sixes}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text(String.format("%.1f", batsman.strikeRate), fontWeight = FontWeight.Bold, color = CricGreen, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
                    }
                }

                // Totals
                item {
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL SCORE", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(scorecard.totalScore, fontWeight = FontWeight.Black, color = CricGreen, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Bowler Table Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BOWLING FIGURES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("BOWLER", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(3f))
                            Text("O", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("M", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("R", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                            Text("W", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = CricRed, modifier = Modifier.weight(1f))
                            Text("EC", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                        }
                    }
                }

                // Bowler Scores
                items(scorecard.bowlers) { bowler ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bowler.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(3f))
                        Text(String.format("%.1f", bowler.overs), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${bowler.maidens}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${bowler.runs}", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text("${bowler.wickets}", fontWeight = FontWeight.Black, color = CricRed, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                        Text(String.format("%.2f", bowler.economy), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun CommentaryTabContent(commentaryState: Resource<List<Commentary>>) {
    when (commentaryState) {
        is Resource.Loading -> LoadingSkeletonList()
        is Resource.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(commentaryState.message) }
        is Resource.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(commentaryState.data) { commentary ->
                    val containerColor = when {
                        commentary.isWicket -> CricRed.copy(alpha = 0.08f)
                        commentary.isBoundary -> CricGold.copy(alpha = 0.08f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val highlightBorder = when {
                        commentary.isWicket -> CricRed
                        commentary.isBoundary -> CricGold
                        else -> Color.Transparent
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = if (highlightBorder != Color.Transparent) androidx.compose.foundation.BorderStroke(1.dp, highlightBorder) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Over & Ball Circle
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            commentary.isWicket -> CricRed
                                            commentary.isBoundary -> CricGold
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            ) {
                                Text(
                                    text = commentary.runText,
                                    color = if (commentary.isWicket || commentary.isBoundary) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Ov ${commentary.overBall}",
                                    color = if (commentary.isWicket || commentary.isBoundary) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }

                            // Commentary description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${commentary.batsman} to ${commentary.bowler}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (commentary.isWicket) CricRed else if (commentary.isBoundary) CricGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = commentary.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
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
fun InfoTabContent(match: Match) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MATCH INFORMATION",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow("Match", match.title)
                InfoRow("Series", match.seriesName)
                InfoRow("Date/Time", "${match.date} at ${match.time}")
                InfoRow("Venue", match.venue)
                InfoRow("Status", match.status)
                InfoRow("Target Score", if (match.target.isNotEmpty()) "${match.target} runs" else "N/A")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f).padding(start = 16.dp)
        )
    }
}

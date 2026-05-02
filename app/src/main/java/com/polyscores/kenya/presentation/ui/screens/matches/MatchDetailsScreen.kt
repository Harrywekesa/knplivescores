package com.polyscores.kenya.presentation.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar

import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    match: Match,
    events: List<MatchEvent>,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    homeTeam: Team?,
    awayTeam: Team?,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(match.homeTeamLogo) {
        if (match.homeTeamLogo.isNotEmpty()) {
            val request = ImageRequest.Builder(context)
                .data(match.homeTeamLogo)
                .allowHardware(false) // Needed for Palette API
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    Palette.from(bitmap).generate { palette ->
                        palette?.dominantSwatch?.rgb?.let { colorInt ->
                            dominantColor = Color(colorInt).copy(alpha = 0.2f)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Match Details",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(dominantColor, MaterialTheme.colorScheme.background),
                        startY = 0f,
                        endY = 800f
                    )
                )
                .padding(paddingValues)
        ) {
            // Header Scoreboard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(match.homeTeamName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val now = System.currentTimeMillis()
                            val isTentative = match.matchStatus == MatchStatus.LIVE && (now - match.lastUpdated.toDate().time) < 180000
                            val homeScoreColor = if (isTentative && match.lastScoringTeamId == match.homeTeamId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                            val awayScoreColor = if (isTentative && match.lastScoringTeamId == match.awayTeamId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer

                            if (match.matchStatus == MatchStatus.POSTPONED) {
                                Text(
                                    text = "POSTPONED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = match.homeScore.toString(),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = homeScoreColor
                                    )
                                    Text(
                                        text = " - ",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = match.awayScore.toString(),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = awayScoreColor
                                    )
                                }
                                Text(
                                    text = match.matchStatus.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            if (match.refereeName.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ref: ${match.refereeName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Text(match.awayTeamName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }

                    val importantEvents = events.filter { 
                        it.eventType == com.polyscores.kenya.data.model.MatchEventType.GOAL || 
                        it.eventType == com.polyscores.kenya.data.model.MatchEventType.PENALTY_GOAL || 
                        it.eventType == com.polyscores.kenya.data.model.MatchEventType.OWN_GOAL || 
                        it.eventType == com.polyscores.kenya.data.model.MatchEventType.YELLOW_CARD || 
                        it.eventType == com.polyscores.kenya.data.model.MatchEventType.RED_CARD 
                    }
                    if (importantEvents.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                importantEvents.filter { it.teamId == match.homeTeamId }.forEach { event ->
                                    val icon = when (event.eventType) {
                                        com.polyscores.kenya.data.model.MatchEventType.GOAL -> "⚽"
                                        com.polyscores.kenya.data.model.MatchEventType.PENALTY_GOAL -> "⚽ (P)"
                                        com.polyscores.kenya.data.model.MatchEventType.OWN_GOAL -> "⚽ (OG)"
                                        com.polyscores.kenya.data.model.MatchEventType.YELLOW_CARD -> "🟨"
                                        com.polyscores.kenya.data.model.MatchEventType.RED_CARD -> "🟥"
                                        else -> ""
                                    }
                                    Text(
                                        text = "$icon ${event.playerName} ${event.minute}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                importantEvents.filter { it.teamId == match.awayTeamId }.forEach { event ->
                                    val icon = when (event.eventType) {
                                        com.polyscores.kenya.data.model.MatchEventType.GOAL -> "⚽"
                                        com.polyscores.kenya.data.model.MatchEventType.PENALTY_GOAL -> "⚽ (P)"
                                        com.polyscores.kenya.data.model.MatchEventType.OWN_GOAL -> "⚽ (OG)"
                                        com.polyscores.kenya.data.model.MatchEventType.YELLOW_CARD -> "🟨"
                                        com.polyscores.kenya.data.model.MatchEventType.RED_CARD -> "🟥"
                                        else -> ""
                                    }
                                    Text(
                                        text = "${event.minute}' ${event.playerName} $icon",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Timeline", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Lineups", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Stats", modifier = Modifier.padding(16.dp))
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> MatchTimeline(events = events, match = match)
                1 -> MatchLineups(match = match, homePlayers = homePlayers, awayPlayers = awayPlayers, homeTeam = homeTeam, awayTeam = awayTeam)
                2 -> com.polyscores.kenya.presentation.ui.components.MatchStatsTab(match = match)
            }
        }
    }
}

@Composable
fun MatchTimeline(events: List<MatchEvent>, match: Match) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No events recorded yet.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                val isHomeEvent = event.teamId == match.homeTeamId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isHomeEvent) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val eventText = when (event.eventType) {
                                com.polyscores.kenya.data.model.MatchEventType.GOAL -> "⚽"
                                com.polyscores.kenya.data.model.MatchEventType.PENALTY_GOAL -> "⚽ (P)"
                                com.polyscores.kenya.data.model.MatchEventType.OWN_GOAL -> "⚽ (OG)"
                                com.polyscores.kenya.data.model.MatchEventType.YELLOW_CARD -> "🟨"
                                com.polyscores.kenya.data.model.MatchEventType.RED_CARD -> "🟥"
                                else -> "(${event.eventType.name})"
                            }

                            if (isHomeEvent) {
                                Text("${event.minute}'", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${event.playerName} $eventText")
                            } else {
                                Text("$eventText ${event.playerName}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${event.minute}'", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchLineups(
    match: Match, 
    homePlayers: List<Player>, 
    awayPlayers: List<Player>,
    homeTeam: Team?,
    awayTeam: Team?
) {
    var showHome by remember { mutableStateOf(true) }

    val currentPlayers = if (showHome) homePlayers else awayPlayers
    val currentTeam = if (showHome) homeTeam else awayTeam
    val currentStartingIds = if (showHome) match.homeStartingXI else match.awayStartingXI
    val currentBenchIds = if (showHome) match.homeBench else match.awayBench
    val teamColorHex = currentTeam?.primaryColor ?: "#1E88E5"
    val teamColor = try { Color(android.graphics.Color.parseColor(teamColorHex)) } catch (e: Exception) { Color(0xFF1E88E5) }

    val startingPlayers = currentStartingIds.mapNotNull { id -> currentPlayers.find { it.id == id } }
    val benchPlayers = currentBenchIds.mapNotNull { id -> currentPlayers.find { it.id == id } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Toggle Switch
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row {
                    TextButton(
                        onClick = { showHome = true },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (showHome) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (showHome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                    ) {
                        Text(match.homeTeamName, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    TextButton(
                        onClick = { showHome = false },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (!showHome) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (!showHome) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    ) {
                        Text(match.awayTeamName, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                if (startingPlayers.isNotEmpty()) {
                    com.polyscores.kenya.presentation.ui.components.VisualPitchLineup(
                        players = startingPlayers,
                        formation = currentTeam?.formation ?: "4-3-3",
                        primaryColor = teamColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.7f).background(Color.LightGray, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Text("Starting XI not released yet.")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Substitutes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                HorizontalDivider()
            }
            
            items(benchPlayers) { player ->
                ListItem(
                    headlineContent = { Text(player.name) },
                    supportingContent = { Text(player.position.name) },
                    leadingContent = {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (player.jerseyNumber > 0) player.jerseyNumber.toString() else "-",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

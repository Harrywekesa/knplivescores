package com.polyscores.kenya.presentation.ui.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.MatchEventType
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import com.polyscores.kenya.presentation.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    player: Player?,
    team: Team?,
    events: List<MatchEvent>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Player Profile",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (player == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val totalGoals = events.count { it.eventType == MatchEventType.GOAL || it.eventType == MatchEventType.PENALTY_GOAL }
        val totalAssists = events.count { it.assistPlayerId == player.id }
        val totalYellows = events.count { it.eventType == MatchEventType.YELLOW_CARD }
        val totalReds = events.count { it.eventType == MatchEventType.RED_CARD }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                PlayerHeader(player = player, team = team)
            }

            // Stats Summary
            item {
                PlayerStatsSummary(
                    goals = totalGoals,
                    assists = totalAssists,
                    yellows = totalYellows,
                    reds = totalReds
                )
            }

            // Timeline
            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            }
            
            val recentEvents = events.sortedByDescending { it.timestamp }.take(15)
            if (recentEvents.isEmpty()) {
                item {
                    EmptyState(message = "No match events recorded for this player yet.")
                }
            } else {
                items(recentEvents) { event ->
                    PlayerEventItem(event = event)
                }
            }
        }
    }
}

@Composable
fun PlayerHeader(player: Player, team: Team?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jersey Number Bubble
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (player.jerseyNumber > 0) player.jerseyNumber.toString() else "-",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${player.position.name} • ${player.age} yrs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                if (team != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (team.logoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = team.logoUrl,
                                contentDescription = "Team Logo",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = team.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerStatsSummary(goals: Int, assists: Int, yellows: Int, reds: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBox(title = "Goals", value = goals.toString(), icon = "⚽", modifier = Modifier.weight(1f))
        StatBox(title = "Assists", value = assists.toString(), icon = "👟", modifier = Modifier.weight(1f))
        StatBox(title = "Yellows", value = yellows.toString(), icon = "🟨", modifier = Modifier.weight(1f))
        StatBox(title = "Reds", value = reds.toString(), icon = "🟥", modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatBox(title: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PlayerEventItem(event: MatchEvent) {
    val icon = when (event.eventType) {
        MatchEventType.GOAL -> "⚽"
        MatchEventType.PENALTY_GOAL -> "⚽ (P)"
        MatchEventType.OWN_GOAL -> "⚽ (OG)"
        MatchEventType.YELLOW_CARD -> "🟨"
        MatchEventType.RED_CARD -> "🟥"
        MatchEventType.SUBSTITUTION_IN -> "⬆️"
        MatchEventType.SUBSTITUTION_OUT -> "⬇️"
        else -> "📌"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.eventType.name.replace("_", " "), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                if (event.description.isNotEmpty()) {
                    Text(event.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${event.minute}'", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

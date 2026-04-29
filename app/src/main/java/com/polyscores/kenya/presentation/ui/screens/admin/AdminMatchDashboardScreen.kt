package com.polyscores.kenya.presentation.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchEventType
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.data.model.Player
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMatchDashboardScreen(
    match: Match,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onUpdateStatus: (MatchStatus) -> Unit,
    onUpdateLineups: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    onDeleteMatch: () -> Unit,
    onAddEvent: (MatchEventType, String, String, String, Int, Boolean) -> Unit,
    onUpdateAnalytics: (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "${match.homeTeamName} vs ${match.awayTeamName}",
                showBackButton = true,
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Match",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Match Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(text = "Status: ${match.matchStatus.name}")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (match.matchStatus != MatchStatus.SECOND_HALF) {
                    Button(
                        onClick = {
                            val newStatus = when (match.matchStatus) {
                                MatchStatus.SCHEDULED -> MatchStatus.LIVE
                                MatchStatus.LIVE -> MatchStatus.HALFTIME
                                MatchStatus.HALFTIME -> MatchStatus.SECOND_HALF
                                else -> MatchStatus.FULLTIME
                            }
                            onUpdateStatus(newStatus)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (match.matchStatus) {
                                MatchStatus.SCHEDULED -> "Start Match"
                                MatchStatus.LIVE -> "Halftime"
                                MatchStatus.HALFTIME -> "Start 2nd Half"
                                else -> "End Match"
                            }
                        )
                    }
                }

                if (match.matchStatus == MatchStatus.LIVE || match.matchStatus == MatchStatus.HALFTIME || match.matchStatus == MatchStatus.SECOND_HALF) {
                    Button(
                        onClick = { onUpdateStatus(MatchStatus.FULLTIME) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("End Match")
                    }
                }

                if (match.matchStatus == MatchStatus.SCHEDULED) {
                    OutlinedButton(
                        onClick = { onUpdateStatus(MatchStatus.POSTPONED) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Postpone")
                    }
                }
            }

            if (match.matchStatus == MatchStatus.SCHEDULED) {
                LineupBuilder(
                    match = match,
                    homePlayers = homePlayers,
                    awayPlayers = awayPlayers,
                    onSaveLineups = onUpdateLineups,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            } else if (match.matchStatus == MatchStatus.LIVE || match.matchStatus == MatchStatus.SECOND_HALF || match.matchStatus == MatchStatus.EXTRA_TIME) {
                MatchEventBuilder(
                    match = match,
                    homePlayers = homePlayers,
                    awayPlayers = awayPlayers,
                    onSaveEvent = onAddEvent
                )
            } else if (match.matchStatus == MatchStatus.HALFTIME) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Match is currently in Half Time.\nEvents cannot be recorded.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (match.matchStatus == MatchStatus.FULLTIME) {
                MatchAnalyticsEditor(
                    match = match,
                    onSaveAnalytics = { hP, aP, hS, aS, hSoT, aSoT, hC, aC, hF, aF ->
                        onUpdateAnalytics(hP, aP, hS, aS, hSoT, aSoT, hC, aC, hF, aF)
                        onBackClick()
                    }
                )
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Match") },
                text = { Text("Are you sure you want to permanently delete this match? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteMatch()
                            showDeleteConfirmDialog = false
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun LineupBuilder(
    match: Match,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onSaveLineups: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTeamTab by remember { mutableStateOf(0) }
    
    // Maps of Player ID to Role (0 = Out, 1 = Start, 2 = Bench)
    val homeLineupMap = remember { mutableStateMapOf<String, Int>().apply {
        homePlayers.forEach { player ->
            put(player.id, when {
                match.homeStartingXI.contains(player.id) -> 1
                match.homeBench.contains(player.id) -> 2
                else -> 0
            })
        }
    } }
    
    val awayLineupMap = remember { mutableStateMapOf<String, Int>().apply {
        awayPlayers.forEach { player ->
            put(player.id, when {
                match.awayStartingXI.contains(player.id) -> 1
                match.awayBench.contains(player.id) -> 2
                else -> 0
            })
        }
    } }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTeamTab) {
            Tab(selected = selectedTeamTab == 0, onClick = { selectedTeamTab = 0 }) {
                Text(match.homeTeamName, modifier = Modifier.padding(16.dp))
            }
            Tab(selected = selectedTeamTab == 1, onClick = { selectedTeamTab = 1 }) {
                Text(match.awayTeamName, modifier = Modifier.padding(16.dp))
            }
        }

        val players = if (selectedTeamTab == 0) homePlayers else awayPlayers
        val lineupMap = if (selectedTeamTab == 0) homeLineupMap else awayLineupMap

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(players) { player ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${player.jerseyNumber}. ${player.name}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    val role = lineupMap[player.id] ?: 0
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = role == 1,
                            onClick = { lineupMap[player.id] = 1 },
                            label = { Text("Start") }
                        )
                        FilterChip(
                            selected = role == 2,
                            onClick = { lineupMap[player.id] = 2 },
                            label = { Text("Bench") }
                        )
                        FilterChip(
                            selected = role == 0,
                            onClick = { lineupMap[player.id] = 0 },
                            label = { Text("Out") }
                        )
                    }
                }
                HorizontalDivider()
            }
        }
        
        Button(
            onClick = {
                val hStart = homeLineupMap.filter { it.value == 1 }.keys.toList()
                val hBench = homeLineupMap.filter { it.value == 2 }.keys.toList()
                val aStart = awayLineupMap.filter { it.value == 1 }.keys.toList()
                val aBench = awayLineupMap.filter { it.value == 2 }.keys.toList()
                onSaveLineups(hStart, hBench, aStart, aBench)
                android.widget.Toast.makeText(context, "Lineups Saved!", android.widget.Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Text("Save Lineups")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchEventBuilder(
    match: Match,
    homePlayers: List<Player>,
    awayPlayers: List<Player>,
    onSaveEvent: (MatchEventType, String, String, String, Int, Boolean) -> Unit
) {
    var isHomeTeam by remember { mutableStateOf(true) }
    var selectedEventType by remember { mutableStateOf(MatchEventType.GOAL) }
    var expandedEventType by remember { mutableStateOf(false) }
    var selectedPlayer by remember { mutableStateOf<Player?>(null) }
    var expandedPlayer by remember { mutableStateOf(false) }
    var minuteString by remember { mutableStateOf("") }

    val currentPlayers = if (isHomeTeam) homePlayers else awayPlayers

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Record Match Event", style = MaterialTheme.typography.titleMedium)
            
            // Team Selection
            TabRow(selectedTabIndex = if (isHomeTeam) 0 else 1) {
                Tab(selected = isHomeTeam, onClick = { isHomeTeam = true; selectedPlayer = null }) {
                    Text(match.homeTeamName, modifier = Modifier.padding(8.dp))
                }
                Tab(selected = !isHomeTeam, onClick = { isHomeTeam = false; selectedPlayer = null }) {
                    Text(match.awayTeamName, modifier = Modifier.padding(8.dp))
                }
            }

            // Event Type
            ExposedDropdownMenuBox(
                expanded = expandedEventType,
                onExpandedChange = { expandedEventType = it }
            ) {
                OutlinedTextField(
                    value = selectedEventType.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEventType) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedEventType,
                    onDismissRequest = { expandedEventType = false }
                ) {
                    MatchEventType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = { selectedEventType = type; expandedEventType = false }
                        )
                    }
                }
            }

            // Player Selection
            ExposedDropdownMenuBox(
                expanded = expandedPlayer,
                onExpandedChange = { expandedPlayer = it }
            ) {
                OutlinedTextField(
                    value = selectedPlayer?.name ?: "Select Player",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Player") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlayer) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedPlayer,
                    onDismissRequest = { expandedPlayer = false }
                ) {
                    currentPlayers.forEach { player ->
                        DropdownMenuItem(
                            text = { Text("${player.jerseyNumber}. ${player.name}") },
                            onClick = { selectedPlayer = player; expandedPlayer = false }
                        )
                    }
                }
            }

            // Minute
            OutlinedTextField(
                value = minuteString,
                onValueChange = { minuteString = it.filter { char -> char.isDigit() } },
                label = { Text("Minute (Auto-captured if empty)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (selectedPlayer != null) {
                        val teamId = if (isHomeTeam) match.homeTeamId else match.awayTeamId
                        val finalMinute = minuteString.toIntOrNull() ?: calculateCurrentMatchMinute(match)
                        onSaveEvent(
                            selectedEventType,
                            teamId,
                            selectedPlayer!!.id,
                            selectedPlayer!!.name,
                            finalMinute,
                            isHomeTeam
                        )
                        // Reset form
                        minuteString = ""
                        selectedPlayer = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedPlayer != null
            ) {
                Text("Save Event")
            }
        }
    }
}

@Composable
fun MatchAnalyticsEditor(
    match: Match,
    onSaveAnalytics: (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
) {
    var homePoss by remember { mutableStateOf(match.homePossession) }
    var awayPoss by remember { mutableStateOf(match.awayPossession) }
    var homeShots by remember { mutableStateOf(match.homeShots) }
    var awayShots by remember { mutableStateOf(match.awayShots) }
    var homeShotsOnTarget by remember { mutableStateOf(match.homeShotsOnTarget) }
    var awayShotsOnTarget by remember { mutableStateOf(match.awayShotsOnTarget) }
    var homeCorners by remember { mutableStateOf(match.homeCorners) }
    var awayCorners by remember { mutableStateOf(match.awayCorners) }
    var homeFouls by remember { mutableStateOf(match.homeFouls) }
    var awayFouls by remember { mutableStateOf(match.awayFouls) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Match Analytics (Full Time)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(match.homeTeamName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Stat", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text(match.awayTeamName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            AnalyticsRow("Possession %", homePoss, awayPoss, { homePoss = it; awayPoss = 100 - it }, { awayPoss = it; homePoss = 100 - it })
            AnalyticsRow("Total Shots", homeShots, awayShots, { homeShots = it }, { awayShots = it })
            AnalyticsRow("Shots on Target", homeShotsOnTarget, awayShotsOnTarget, { homeShotsOnTarget = it }, { awayShotsOnTarget = it })
            AnalyticsRow("Corners", homeCorners, awayCorners, { homeCorners = it }, { awayCorners = it })
            AnalyticsRow("Fouls", homeFouls, awayFouls, { homeFouls = it }, { awayFouls = it })

            Button(
                onClick = { onSaveAnalytics(homePoss, awayPoss, homeShots, awayShots, homeShotsOnTarget, awayShotsOnTarget, homeCorners, awayCorners, homeFouls, awayFouls) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save Analytics")
            }
        }
    }
}

@Composable
fun AnalyticsRow(label: String, homeVal: Int, awayVal: Int, onHomeChange: (Int) -> Unit, onAwayChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = homeVal.toString(),
            onValueChange = { it.toIntOrNull()?.let { v -> onHomeChange(v) } },
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
        Text(label, modifier = Modifier.weight(1f).padding(horizontal = 4.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = awayVal.toString(),
            onValueChange = { it.toIntOrNull()?.let { v -> onAwayChange(v) } },
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )
    }
}

private fun calculateCurrentMatchMinute(match: Match): Int {
    val now = System.currentTimeMillis()
    return when (match.matchStatus) {
        MatchStatus.LIVE -> {
            val start = match.startTime?.toDate()?.time
            if (start != null) {
                ((now - start) / 60000).coerceAtLeast(1).toInt()
            } else 1
        }
        MatchStatus.SECOND_HALF -> {
            val start = match.secondHalfStartTime?.toDate()?.time
            if (start != null) {
                (45 + ((now - start) / 60000)).coerceAtLeast(46).toInt()
            } else 46
        }
        MatchStatus.EXTRA_TIME -> 90
        else -> 0
    }
}

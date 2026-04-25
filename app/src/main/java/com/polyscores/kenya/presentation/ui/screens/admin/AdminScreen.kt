@file:OptIn(ExperimentalMaterial3Api::class)

package com.polyscores.kenya.presentation.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import com.polyscores.kenya.data.model.Match
import com.polyscores.kenya.data.model.MatchStatus
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import com.polyscores.kenya.presentation.ui.theme.PolyScoresTheme

@Composable
fun AdminScreen(
    matches: List<Match>,
    activeDeviceCount: Int = 0,
    onUpdateScoreClick: (Match) -> Unit,
    onAddMatchClick: () -> Unit,
    onManageTeamsClick: () -> Unit,
    onManageLeaguesClick: () -> Unit,
    onEditStandingsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Admin Panel",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Admin Actions
            AdminActionsSection(
                onAddMatch = onAddMatchClick,
                onManageTeams = onManageTeamsClick,
                onManageLeagues = onManageLeaguesClick,
                onEditStandings = onEditStandingsClick
            )

            // Active Devices Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Active Devices",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Live Active Fans",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$activeDeviceCount Connected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Live Matches Requiring Updates
            Text(
                text = "Manage Matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )

            val activeMatches = matches.filter { 
                it.matchStatus == MatchStatus.LIVE || 
                it.matchStatus == MatchStatus.HALFTIME || 
                it.matchStatus == MatchStatus.SECOND_HALF ||
                it.matchStatus == MatchStatus.SCHEDULED || 
                it.matchStatus == MatchStatus.EXTRA_TIME || 
                it.matchStatus == MatchStatus.PENALTY_SHOOTOUT 
            }

            if (activeMatches.isEmpty()) {
                Text(
                    text = "No matches available to manage. Add a match first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(activeMatches) { match ->
                        AdminMatchCard(
                            match = match,
                            onClick = { onUpdateScoreClick(match) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminActionsSection(
    onAddMatch: () -> Unit,
    onManageTeams: () -> Unit,
    onManageLeagues: () -> Unit,
    onEditStandings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionButton(
                icon = Icons.Default.Add,
                label = "Add Match",
                onClick = onAddMatch,
                modifier = Modifier.weight(1f)
            )
            AdminActionButton(
                icon = Icons.Default.People,
                label = "Teams",
                onClick = onManageTeams,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionButton(
                icon = Icons.Default.SportsSoccer,
                label = "Leagues",
                onClick = onManageLeagues,
                modifier = Modifier.weight(1f)
            )
            AdminActionButton(
                icon = Icons.Default.FormatListNumbered,
                label = "Standings",
                onClick = onEditStandings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AdminActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(80.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AdminMatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Team
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = match.homeTeamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }

            // Score Box
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val now = System.currentTimeMillis()
                    val isTentative = match.matchStatus == MatchStatus.LIVE && (now - match.lastUpdated.toDate().time) < 180000
                    val homeScoreColor = if (isTentative && match.lastScoringTeamId == match.homeTeamId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    val awayScoreColor = if (isTentative && match.lastScoringTeamId == match.awayTeamId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer

                    Row {
                        Text(
                            text = match.homeScore.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = homeScoreColor
                        )
                        Text(
                            text = " - ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = match.awayScore.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = awayScoreColor
                        )
                    }
                    if (match.matchStatus == MatchStatus.LIVE || match.matchStatus == MatchStatus.HALFTIME || match.matchStatus == MatchStatus.SECOND_HALF) {
                        com.polyscores.kenya.presentation.ui.components.LiveMatchTimer(match = match)
                    } else {
                        Text(
                            text = getMatchStatusText(match.matchStatus),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Away Team
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = match.awayTeamName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
        }
    }
}

private fun getMatchStatusText(status: MatchStatus): String {
    return when (status) {
        MatchStatus.SCHEDULED -> "Scheduled"
        MatchStatus.LIVE -> "Live"
        MatchStatus.HALFTIME -> "Half Time"
        MatchStatus.SECOND_HALF -> "2nd Half"
        MatchStatus.FULLTIME -> "Full Time"
        MatchStatus.EXTRA_TIME -> "Extra Time"
        MatchStatus.PENALTY_SHOOTOUT -> "Penalties"
        MatchStatus.POSTPONED -> "Postponed"
        MatchStatus.CANCELLED -> "Cancelled"
        MatchStatus.AWARDED -> "Awarded"
    }
}

// ==================== UPDATE SCORE DIALOG ====================

@Composable
fun UpdateScoreDialog(
    match: Match,
    onDismiss: () -> Unit,
    onUpdateScore: (Int, Int) -> Unit,
    onUpdateStatus: (MatchStatus) -> Unit
) {
    var homeScore by remember { mutableStateOf(match.homeScore.toString()) }
    var awayScore by remember { mutableStateOf(match.awayScore.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Score",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "${match.homeTeamName} vs ${match.awayTeamName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home Score
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = match.homeTeamName,
                            style = MaterialTheme.typography.labelMedium
                        )
                        OutlinedTextField(
                            value = homeScore,
                            onValueChange = { if (it.all { c -> c.isDigit() }) homeScore = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Home") },
                            singleLine = true
                        )
                    }

                    Text(
                        text = "-",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // Away Score
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = match.awayTeamName,
                            style = MaterialTheme.typography.labelMedium
                        )
                        OutlinedTextField(
                            value = awayScore,
                            onValueChange = { if (it.all { c -> c.isDigit() }) awayScore = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Away") },
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Match Status
                Text(
                    text = "Match Status",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MatchStatusChip(
                        status = MatchStatus.LIVE,
                        currentStatus = match.matchStatus,
                        onClick = onUpdateStatus
                    )
                    MatchStatusChip(
                        status = MatchStatus.HALFTIME,
                        currentStatus = match.matchStatus,
                        onClick = onUpdateStatus
                    )
                    MatchStatusChip(
                        status = MatchStatus.FULLTIME,
                        currentStatus = match.matchStatus,
                        onClick = onUpdateStatus
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hScore = homeScore.toIntOrNull() ?: 0
                    val aScore = awayScore.toIntOrNull() ?: 0
                    onUpdateScore(hScore, aScore)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MatchStatusChip(
    status: MatchStatus,
    currentStatus: MatchStatus,
    onClick: (MatchStatus) -> Unit
) {
    FilterChip(
        selected = status == currentStatus,
        onClick = { onClick(status) },
        label = { Text(status.name) },
        leadingIcon = if (status == currentStatus) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}

// ==================== ADD MATCH DIALOG ====================

@Composable
fun AddMatchDialog(
    teams: List<String>,
    onDismiss: () -> Unit,
    onAddMatch: (String, String, String, String, Long, String) -> Unit
) {
    var selectedHomeTeam by remember { mutableStateOf("") }
    var selectedAwayTeam by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("Main Field") }
    var round by remember { mutableStateOf("Round 1") }
    var referee by remember { mutableStateOf("") }
    var expandedHome by remember { mutableStateOf(false) }
    var expandedAway by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dateString by remember { mutableStateOf("Select Date") }
    var timeString by remember { mutableStateOf("Select Time") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                dateString = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                timeString = String.format("%02d:%02d", hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Match") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Home Team Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedHome,
                    onExpandedChange = { expandedHome = it }
                ) {
                    OutlinedTextField(
                        value = selectedHomeTeam,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Home Team") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHome) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedHome,
                        onDismissRequest = { expandedHome = false }
                    ) {
                        teams.forEach { team ->
                            DropdownMenuItem(
                                text = { Text(team) },
                                onClick = {
                                    selectedHomeTeam = team
                                    expandedHome = false
                                }
                            )
                        }
                    }
                }

                // Away Team Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedAway,
                    onExpandedChange = { expandedAway = it }
                ) {
                    OutlinedTextField(
                        value = selectedAwayTeam,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Away Team") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAway) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAway,
                        onDismissRequest = { expandedAway = false }
                    ) {
                        teams.forEach { team ->
                            DropdownMenuItem(
                                text = { Text(team) },
                                onClick = {
                                    selectedAwayTeam = team
                                    expandedAway = false
                                }
                            )
                        }
                    }
                }

                // Venue
                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Venue") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Round
                OutlinedTextField(
                    value = round,
                    onValueChange = { round = it },
                    label = { Text("Round/Stage") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Referee
                OutlinedTextField(
                    value = referee,
                    onValueChange = { referee = it },
                    label = { Text("Referee Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Date & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dateString)
                    }
                    OutlinedButton(
                        onClick = { timePickerDialog.show() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeString)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    val selectedTime = calendar.timeInMillis
                    
                    if (selectedHomeTeam.isEmpty() || selectedAwayTeam.isEmpty()) {
                        errorMessage = "Please select both teams."
                    } else if (selectedHomeTeam == selectedAwayTeam) {
                        errorMessage = "A team cannot play against itself."
                    } else if (selectedTime < currentTime) {
                        errorMessage = "A match cannot start in the past."
                    } else {
                        errorMessage = null
                        onAddMatch(selectedHomeTeam, selectedAwayTeam, venue, round, selectedTime, referee)
                    }
                }
            ) {
                Text("Add Match")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==================== MANAGE TEAMS DIALOG ====================

@Composable
fun ManageTeamsDialog(
    onDismiss: () -> Unit,
    onCreateTeam: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var coach by remember { mutableStateOf("") }
    var formation by remember { mutableStateOf("4-3-3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Team") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = coach,
                    onValueChange = { coach = it },
                    label = { Text("Coach") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formation,
                    onValueChange = { formation = it },
                    label = { Text("Formation (e.g. 4-3-3)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateTeam(name, department, coach, formation)
                    }
                }
            ) {
                Text("Create Team")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==================== MANAGE LEAGUES DIALOG ====================

@Composable
fun ManageLeaguesDialog(
    onDismiss: () -> Unit,
    onCreateLeague: (String, String, String, String, String, Long, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("2024/2025") }
    var about by remember { mutableStateOf("") }
    var rules by remember { mutableStateOf("") }
    var prizes by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val calendar = remember { java.util.Calendar.getInstance() }
    
    var startDateString by remember { mutableStateOf("Select Start Date") }
    var endDateString by remember { mutableStateOf("Select End Date") }
    var startTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTimestamp by remember { mutableStateOf(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) }

    val startDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                startTimestamp = calendar.timeInMillis
                startDateString = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val endDatePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                endTimestamp = calendar.timeInMillis
                endDateString = "$dayOfMonth/${month + 1}/$year"
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New League") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("League Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = season,
                    onValueChange = { season = it },
                    label = { Text("Season") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = startDateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { startDatePickerDialog.show() })
                }
                Box {
                    OutlinedTextField(
                        value = endDateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("End Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { endDatePickerDialog.show() })
                }
                OutlinedTextField(
                    value = about,
                    onValueChange = { about = it },
                    label = { Text("About (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rules,
                    onValueChange = { rules = it },
                    label = { Text("Rules (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prizes,
                    onValueChange = { prizes = it },
                    label = { Text("Prizes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreateLeague(name, season, about, rules, prizes, startTimestamp, endTimestamp)
                    }
                }
            ) {
                Text("Create League")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    val sampleMatches = listOf(
        Match(
            id = "1",
            homeTeamName = "Engineering",
            awayTeamName = "Business",
            homeScore = 2,
            awayScore = 1,
            matchStatus = MatchStatus.LIVE,
            venue = "Main Field"
        ),
        Match(
            id = "2",
            homeTeamName = "Science",
            awayTeamName = "Arts",
            homeScore = 0,
            awayScore = 0,
            matchStatus = MatchStatus.HALFTIME,
            venue = "Field B"
        )
    )
    PolyScoresTheme {
        AdminScreen(
            matches = sampleMatches,
            onUpdateScoreClick = {},
            onAddMatchClick = {},
            onManageTeamsClick = {},
            onManageLeaguesClick = {},
            onEditStandingsClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateScoreDialogPreview() {
    val sampleMatch = Match(
        id = "1",
        homeTeamName = "Engineering",
        awayTeamName = "Business",
        homeScore = 2,
        awayScore = 1,
        matchStatus = MatchStatus.LIVE,
        venue = "Main Field"
    )
    PolyScoresTheme {
        UpdateScoreDialog(
            match = sampleMatch,
            onDismiss = {},
            onUpdateScore = { _, _ -> },
            onUpdateStatus = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddMatchDialogPreview() {
    val sampleTeams = listOf("Engineering", "Business", "Science", "Arts")
    PolyScoresTheme {
        AddMatchDialog(
            teams = sampleTeams,
            onDismiss = {},
            onAddMatch = { _, _, _, _, _, _ -> }
        )
    }
}

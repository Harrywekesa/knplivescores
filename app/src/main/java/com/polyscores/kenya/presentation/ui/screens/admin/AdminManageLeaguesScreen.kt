package com.polyscores.kenya.presentation.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.League
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageLeaguesScreen(
    leagues: List<League>,
    teams: List<Team>,
    onAddLeagueClick: () -> Unit,
    onDeleteLeagueClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }
    var showTeamsDialogForLeague by remember { mutableStateOf<League?>(null) }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Manage Leagues",
                showBackButton = true,
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLeagueClick) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add League")
            }
        }
    ) { paddingValues ->
        if (leagues.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No leagues available. Add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leagues) { league ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = league.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Season: ${league.season}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { showTeamsDialogForLeague = league }) {
                                    Text("View Teams")
                                }
                                IconButton(onClick = { showDeleteConfirmDialog = league.id }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete League",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                title = { Text("Delete League") },
                text = { Text("Are you sure you want to permanently delete this league? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteLeagueClick(showDeleteConfirmDialog!!)
                            showDeleteConfirmDialog = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        showTeamsDialogForLeague?.let { league ->
            val leagueTeams = teams.filter { league.teamIds.contains(it.id) }
            AlertDialog(
                onDismissRequest = { showTeamsDialogForLeague = null },
                title = { Text("Teams in ${league.name}") },
                text = {
                    if (leagueTeams.isEmpty()) {
                        Text("No teams have been assigned to this league yet.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(leagueTeams) { team ->
                                Text("• ${team.name} (${team.department})")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTeamsDialogForLeague = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

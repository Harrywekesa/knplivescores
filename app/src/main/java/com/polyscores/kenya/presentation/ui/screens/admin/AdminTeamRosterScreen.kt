package com.polyscores.kenya.presentation.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.Team
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar

import com.polyscores.kenya.data.model.League

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeamRosterScreen(
    team: Team,
    players: List<Player>,
    leagues: List<League>,
    onAddPlayerClick: () -> Unit,
    onDeletePlayer: (String) -> Unit,
    onDeleteTeam: (String) -> Unit,
    onAssignToLeague: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showAssignLeagueDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Edit ${team.name}",
                showBackButton = true,
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Team",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlayerClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Player")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Team Roster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Formation: ${team.formation}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            OutlinedButton(
                onClick = { showAssignLeagueDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_agenda),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Assign Team to League")
            }

            if (players.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No players added to this team yet.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                ) {
                    items(players) { player ->
                        AdminPlayerListItem(
                            player = player,
                            onDelete = { onDeletePlayer(player.id) }
                        )
                    }
                }
            }
        }
        
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Team") },
                text = { Text("Are you sure you want to delete ${team.name}? This will also delete all players in this team.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteTeam(team.id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAssignLeagueDialog) {
            AlertDialog(
                onDismissRequest = { showAssignLeagueDialog = false },
                title = { Text("Assign to League") },
                text = {
                    if (leagues.isEmpty()) {
                        Text("No leagues available. Please create a league first.")
                    } else {
                        LazyColumn {
                            items(leagues) { league ->
                                val isAssigned = league.teamIds.contains(team.id)
                                TextButton(
                                    onClick = {
                                        if (!isAssigned) {
                                            onAssignToLeague(league.id)
                                        }
                                        showAssignLeagueDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isAssigned
                                ) {
                                    Text(
                                        text = if (isAssigned) "${league.name} (Assigned)" else league.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isAssigned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAssignLeagueDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminPlayerListItem(
    player: Player,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(player.name, fontWeight = FontWeight.Bold) },
        supportingContent = { Text("Position: ${player.position.name} • Age: ${player.age}") },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (player.jerseyNumber > 0) {
                        Text(
                            text = player.jerseyNumber.toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.isCaptain) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "C",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Player",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
    HorizontalDivider()
}

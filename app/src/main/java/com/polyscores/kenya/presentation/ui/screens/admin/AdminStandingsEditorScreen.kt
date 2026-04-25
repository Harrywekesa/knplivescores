package com.polyscores.kenya.presentation.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.League
import com.polyscores.kenya.data.model.StandingsEntry
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStandingsEditorScreen(
    leagues: List<League>,
    standings: List<StandingsEntry>,
    onLeagueSelected: (String) -> Unit,
    onSaveStandings: (String, List<StandingsEntry>) -> Unit,
    onAutoCalculate: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedLeagueId by remember { mutableStateOf(leagues.firstOrNull()?.id ?: "") }
    var expandedLeagueDropdown by remember { mutableStateOf(false) }

    // Editable state for standings
    var editableStandings by remember(standings) { mutableStateOf(standings) }

    // Whenever league changes, notify parent to load standings
    LaunchedEffect(selectedLeagueId) {
        if (selectedLeagueId.isNotBlank()) {
            onLeagueSelected(selectedLeagueId)
        }
    }

    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = "Edit Standings",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // League Selector
            ExposedDropdownMenuBox(
                expanded = expandedLeagueDropdown,
                onExpandedChange = { expandedLeagueDropdown = it }
            ) {
                val selectedLeagueName = leagues.find { it.id == selectedLeagueId }?.name ?: "Select League"
                OutlinedTextField(
                    value = selectedLeagueName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("League") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLeagueDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedLeagueDropdown,
                    onDismissRequest = { expandedLeagueDropdown = false }
                ) {
                    leagues.forEach { league ->
                        DropdownMenuItem(
                            text = { Text(league.name) },
                            onClick = {
                                selectedLeagueId = league.id
                                expandedLeagueDropdown = false
                            }
                        )
                    }
                }
            }

            if (editableStandings.isEmpty()) {
                Text("No teams found in this league's standings. Play a match to initialize the table.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(editableStandings.size) { index ->
                        val entry = editableStandings[index]
                        Card {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(entry.teamName, style = MaterialTheme.typography.titleMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    NumberField("P", entry.played) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(played = v) } }
                                    NumberField("W", entry.won) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(won = v) } }
                                    NumberField("D", entry.drawn) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(drawn = v) } }
                                    NumberField("L", entry.lost) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(lost = v) } }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    NumberField("GF", entry.goalsFor) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(goalsFor = v) } }
                                    NumberField("GA", entry.goalsAgainst) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(goalsAgainst = v) } }
                                    NumberField("Pts", entry.points) { v -> editableStandings = editableStandings.toMutableList().apply { this[index] = entry.copy(points = v) } }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            // This will trigger the auto-calculate callback
                            // We need a new callback to pass to this screen
                            onAutoCalculate(selectedLeagueId) 
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Auto-Calculate")
                    }
                    Button(
                        onClick = { onSaveStandings(selectedLeagueId, editableStandings) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Standings")
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NumberField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull() ?: 0) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.weight(1f),
        singleLine = true
    )
}

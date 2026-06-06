package com.polyscores.kenya.presentation.ui.screens.standings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.StandingsEntry
import com.polyscores.kenya.presentation.ui.components.PolyScoresTopBar
import com.polyscores.kenya.presentation.ui.components.StandingsTable
import com.polyscores.kenya.presentation.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandingsScreen(
    standings: List<StandingsEntry>,
    topScorers: List<com.polyscores.kenya.data.repository.PlayerStatItem> = emptyList(),
    topAssists: List<com.polyscores.kenya.data.repository.PlayerStatItem> = emptyList(),
    topYellowCards: List<com.polyscores.kenya.data.repository.PlayerStatItem> = emptyList(),
    topRedCards: List<com.polyscores.kenya.data.repository.PlayerStatItem> = emptyList(),
    leagueName: String = "League Table",
    isLoading: Boolean,
    onTeamClick: (String) -> Unit = {},
    onPlayerClick: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    Scaffold(
        topBar = {
            PolyScoresTopBar(
                title = leagueName,
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Standings", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Player Stats", modifier = Modifier.padding(16.dp))
                }
            }

            val pullRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
            var isRefreshing by remember { mutableStateOf(false) }

            LaunchedEffect(isRefreshing) {
                if (isRefreshing) {
                    kotlinx.coroutines.delay(1000)
                    isRefreshing = false
                }
            }

            if (selectedTab == 0) {
                if (standings.isEmpty() && !isLoading) {
                    EmptyState(
                        message = "No standings available yet. Start playing matches to see the table!"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            StandingsTable(
                                standings = standings,
                                onTeamClick = onTeamClick
                            )
                        }
                    }
                }
            } else {
                var statTab by remember { mutableStateOf(0) }
                
                Column(modifier = Modifier.fillMaxSize()) {
                    ScrollableTabRow(
                        selectedTabIndex = statTab,
                        edgePadding = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(selected = statTab == 0, onClick = { statTab = 0 }) { Text("Goals", modifier = Modifier.padding(12.dp)) }
                        Tab(selected = statTab == 1, onClick = { statTab = 1 }) { Text("Assists", modifier = Modifier.padding(12.dp)) }
                        Tab(selected = statTab == 2, onClick = { statTab = 2 }) { Text("Yellow Cards", modifier = Modifier.padding(12.dp)) }
                        Tab(selected = statTab == 3, onClick = { statTab = 3 }) { Text("Red Cards", modifier = Modifier.padding(12.dp)) }
                    }
                    
                    val currentList = when (statTab) {
                        0 -> topScorers
                        1 -> topAssists
                        2 -> topYellowCards
                        else -> topRedCards
                    }
                    val label = when (statTab) {
                        0 -> "Goals"
                        1 -> "Assists"
                        2 -> "Yellows"
                        else -> "Reds"
                    }
                    
                    if (currentList.isEmpty()) {
                        EmptyState(message = "No stats recorded yet.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(currentList.size) { index ->
                                val scorer = currentList[index]
                                ListItem(
                                    modifier = Modifier.clickable { onPlayerClick(scorer.playerId) },
                                    headlineContent = { Text(scorer.playerName, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                    leadingContent = { Text("${index + 1}.") },
                                    trailingContent = { Text("${scorer.statCount} $label") }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

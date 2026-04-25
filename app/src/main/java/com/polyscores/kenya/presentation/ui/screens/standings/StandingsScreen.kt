package com.polyscores.kenya.presentation.ui.screens.standings

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
    topScorers: List<Pair<String, Int>> = emptyList(),
    leagueName: String = "League Table",
    isLoading: Boolean,
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
                    Text("Top Scorers", modifier = Modifier.padding(16.dp))
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
                            StandingsTable(standings = standings)
                        }
                    }
                }
            } else {
                if (topScorers.isEmpty()) {
                    EmptyState(message = "No goals scored yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(topScorers.size) { index ->
                            val scorer = topScorers[index]
                            ListItem(
                                headlineContent = { Text(scorer.first, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                                leadingContent = { Text("${index + 1}.") },
                                trailingContent = { Text("${scorer.second} Goals") }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

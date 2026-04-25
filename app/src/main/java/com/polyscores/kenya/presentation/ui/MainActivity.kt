package com.polyscores.kenya.presentation.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.polyscores.kenya.presentation.ui.screens.admin.AdminScreen
import com.polyscores.kenya.presentation.ui.screens.admin.UpdateScoreDialog
import com.polyscores.kenya.presentation.ui.screens.admin.AddMatchDialog
import com.polyscores.kenya.presentation.ui.screens.admin.ManageTeamsDialog
import com.polyscores.kenya.presentation.ui.screens.admin.ManageLeaguesDialog
import com.polyscores.kenya.presentation.ui.screens.home.HomeScreen
import com.polyscores.kenya.presentation.ui.screens.matches.MatchesScreen
import com.polyscores.kenya.presentation.ui.screens.settings.SettingsScreen
import com.polyscores.kenya.presentation.ui.screens.standings.StandingsScreen
import com.polyscores.kenya.presentation.ui.screens.teams.TeamsScreen
import com.polyscores.kenya.presentation.ui.screens.teams.TeamDetailsScreen
import com.polyscores.kenya.presentation.ui.screens.teams.AddPlayerDialog
import com.polyscores.kenya.presentation.ui.screens.admin.AdminMatchDashboardScreen
import com.polyscores.kenya.presentation.ui.screens.admin.AdminManageLeaguesScreen
import com.polyscores.kenya.presentation.ui.screens.admin.AdminManageTeamsScreen
import com.polyscores.kenya.presentation.ui.screens.admin.AdminStandingsEditorScreen
import com.polyscores.kenya.presentation.ui.screens.admin.AdminTeamRosterScreen
import com.polyscores.kenya.presentation.ui.screens.matches.MatchDetailsScreen
import com.polyscores.kenya.presentation.ui.theme.PolyScoresTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: com.polyscores.kenya.presentation.viewModel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val preferences by settingsViewModel.userPreferences.collectAsState()
            PolyScoresTheme(darkTheme = preferences.darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PolyScoresApp()
                }
            }
        }
    }
}

@Composable
fun PolyScoresApp() {
    val matchesViewModel: com.polyscores.kenya.presentation.viewModel.MatchesViewModel = viewModel()
    val teamsViewModel: com.polyscores.kenya.presentation.viewModel.TeamsViewModel = viewModel()
    val standingsViewModel: com.polyscores.kenya.presentation.viewModel.StandingsViewModel = viewModel()
    val leaguesViewModel: com.polyscores.kenya.presentation.viewModel.LeaguesViewModel = viewModel()
    val settingsViewModel: com.polyscores.kenya.presentation.viewModel.SettingsViewModel = viewModel()
    val authViewModel: com.polyscores.kenya.presentation.viewModel.AuthViewModel = viewModel()
    val presenceViewModel: com.polyscores.kenya.presentation.viewModel.PresenceViewModel = viewModel()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current

    val matches by matchesViewModel.matches.collectAsState()
    val teams by teamsViewModel.teams.collectAsState()
    val leagues by leaguesViewModel.leagues.collectAsState()
    val standings by standingsViewModel.standings.collectAsState()
    val selectedLeagueId by leaguesViewModel.selectedLeagueId.collectAsState()
    val isAdminSession by authViewModel.isAdminSession.collectAsState()

    var showUpdateScoreDialog by remember { mutableStateOf(false) }
    var showAddMatchDialog by remember { mutableStateOf(false) }
    var showManageLeaguesDialog by remember { mutableStateOf(false) }
    var selectedMatch by remember { mutableStateOf<com.polyscores.kenya.data.model.Match?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        matchesViewModel.latestGoalEvent.collect { (match, homeScored) ->
            val scorerTeam = if (homeScored) match.homeTeamName else match.awayTeamName
            val message = "GOAL! $scorerTeam scored! (${match.homeScore} - ${match.awayScore})"
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "View",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                navController.navigate("${Screen.MatchDetails}/${match.id}")
            }
        }
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                // Permission denied
            }
        }
        LaunchedEffect(Unit) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val showBottomBar = remember(navBackStackEntry) {
        navBackStackEntry?.destination?.route in listOf(
            Screen.Home,
            Screen.Matches,
            Screen.Standings,
            Screen.Teams,
            Screen.Settings
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                PolyScoresBottomBar(
                    currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            exitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popEnterTransition = {
                androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popExitTransition = {
                androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            }
        ) {
            composable(Screen.Home) {
                HomeScreen(
                    matches = matches,
                    isLoading = false,
                    onMatchClick = { matchId ->
                        navController.navigate("${Screen.MatchDetails}/$matchId")
                    },
                    onNavigateToMatches = {
                        navController.navigate(Screen.Matches)
                    },
                    onNavigateToStandings = {
                        navController.navigate(Screen.Standings)
                    },
                    onNavigateToTeams = {
                        navController.navigate(Screen.Teams)
                    },
                    onNavigateToAdmin = {
                        if (isAdminSession) {
                            navController.navigate(Screen.Admin)
                        } else {
                            navController.navigate("admin_login")
                        }
                    }
                )
            }

            composable(Screen.Matches) {
                MatchesScreen(
                    matches = matches,
                    isLoading = false,
                    onMatchClick = { matchId ->
                        navController.navigate("${Screen.MatchDetails}/$matchId")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Standings) {
                val topScorers by matchesViewModel.topScorers.collectAsState(initial = emptyList())
                val topScorersWithTeam = topScorers.map { scorer ->
                    val teamName = teams.find { it.id == scorer.second }?.name ?: "Unknown"
                    Pair("${scorer.first} ($teamName)", scorer.third)
                }
                StandingsScreen(
                    standings = standings,
                    topScorers = topScorersWithTeam,
                    isLoading = false,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Teams) {
                TeamsScreen(
                    teams = teams,
                    isLoading = false,
                    onTeamClick = { teamId ->
                        navController.navigate("${Screen.TeamDetails}/$teamId")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("${Screen.TeamDetails}/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val team = teams.find { it.id == teamId }
                
                if (team != null) {
                    val teamPlayers by teamsViewModel.getTeamPlayers(teamId).collectAsState(initial = emptyList())
                    
                    TeamDetailsScreen(
                        team = team,
                        players = teamPlayers,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Settings) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    isAdminSession = isAdminSession,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAdminClick = {
                        if (isAdminSession) {
                            navController.navigate(Screen.Admin)
                        } else {
                            navController.navigate("admin_login")
                        }
                    },
                    onAdminLogoutClick = {
                        authViewModel.logout()
                        android.widget.Toast.makeText(context, "Admin logged out", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }

            composable("admin_login") {
                com.polyscores.kenya.presentation.ui.screens.admin.AdminLoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.popBackStack()
                        navController.navigate(Screen.Admin)
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Admin) {
                val activeDeviceCount by presenceViewModel.activeDeviceCount.collectAsState()
                AdminScreen(
                    matches = matches,
                    activeDeviceCount = activeDeviceCount,
                    onUpdateScoreClick = { match ->
                        navController.navigate("${Screen.AdminMatchDashboard}/${match.id}")
                    },
                    onAddMatchClick = {
                        showAddMatchDialog = true
                    },
                    onManageTeamsClick = {
                        navController.navigate(Screen.AdminManageTeams)
                    },
                    onManageLeaguesClick = {
                        navController.navigate(Screen.AdminManageLeagues)
                    },
                    onEditStandingsClick = {
                        navController.navigate(Screen.AdminStandingsEditor)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable("${Screen.AdminMatchDashboard}/{matchId}") { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                val match = matches.find { it.id == matchId }

                if (match != null) {
                    val homePlayers by remember(match.homeTeamId) { teamsViewModel.getTeamPlayers(match.homeTeamId) }.collectAsState(initial = emptyList())
                    val awayPlayers by remember(match.awayTeamId) { teamsViewModel.getTeamPlayers(match.awayTeamId) }.collectAsState(initial = emptyList())

                    AdminMatchDashboardScreen(
                        match = match,
                        homePlayers = homePlayers,
                        awayPlayers = awayPlayers,
                        onUpdateStatus = { status ->
                            matchesViewModel.updateMatchStatus(match.id, status)
                        },
                        onUpdateLineups = { homeStart, homeBench, awayStart, awayBench ->
                            matchesViewModel.updateMatchLineups(match.id, homeStart, homeBench, awayStart, awayBench)
                        },
                        onDeleteMatch = {
                            matchesViewModel.deleteMatch(match.id, onSuccess = {
                                android.widget.Toast.makeText(context, "Match deleted", android.widget.Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            })
                        },
                        onAddEvent = { eventType: com.polyscores.kenya.data.model.MatchEventType, teamId: String, playerId: String, playerName: String, minute: Int, isHomeTeam: Boolean ->
                            val event = com.polyscores.kenya.data.model.MatchEvent(
                                matchId = match.id,
                                eventType = eventType,
                                teamId = teamId,
                                playerId = playerId,
                                playerName = playerName,
                                minute = minute,
                                description = "$playerName ($minute')"
                            )
                            matchesViewModel.addMatchEvent(event)
                        },
                        onUpdateAnalytics = { hp, ap, hSot, aSot, hC, aC, hF, aF ->
                            matchesViewModel.updateMatchAnalytics(match.id, hp, ap, hSot, aSot, hC, aC, hF, aF)
                            android.widget.Toast.makeText(context, "Analytics Saved", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.AdminManageLeagues) {
                AdminManageLeaguesScreen(
                    leagues = leagues,
                    teams = teams,
                    onAddLeagueClick = { showManageLeaguesDialog = true },
                    onDeleteLeagueClick = { leagueId ->
                        leaguesViewModel.deleteLeague(leagueId)
                        android.widget.Toast.makeText(context, "League deleted", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminStandingsEditor) {
                val currentStandings by standingsViewModel.standings.collectAsState()
                
                LaunchedEffect(selectedLeagueId) {
                    if (selectedLeagueId.isNotEmpty()) {
                        val leagueMatches = matches.filter { it.leagueId == selectedLeagueId }
                        val league = leagues.find { it.id == selectedLeagueId }
                        val leagueTeams = teams.filter { league?.teamIds?.contains(it.id) == true }
                        standingsViewModel.loadStandings(selectedLeagueId, leagueMatches, leagueTeams)
                    }
                }
                
                AdminStandingsEditorScreen(
                    leagues = leagues,
                    standings = currentStandings,
                    onLeagueSelected = { leagueId ->
                        leaguesViewModel.setLeagueId(leagueId)
                    },
                    onSaveStandings = { leagueId, newStandings ->
                        standingsViewModel.updateStandings(leagueId, newStandings, onSuccess = {
                            android.widget.Toast.makeText(context, "Standings saved", android.widget.Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        })
                    },
                    onAutoCalculate = { leagueId ->
                        standingsViewModel.autoCalculateStandings(leagueId, matches, teams, onSuccess = {
                            android.widget.Toast.makeText(context, "Standings Auto-Calculated and Saved", android.widget.Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        })
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminManageTeams) {
                var showManageTeamsDialog by remember { mutableStateOf(false) }
                
                AdminManageTeamsScreen(
                    teams = teams,
                    onTeamClick = { teamId ->
                        navController.navigate("${Screen.AdminTeamRoster}/$teamId")
                    },
                    onCreateTeamClick = {
                        showManageTeamsDialog = true
                    },
                    onBackClick = { navController.popBackStack() }
                )
                
                if (showManageTeamsDialog) {
                    ManageTeamsDialog(
                        onDismiss = { showManageTeamsDialog = false },
                        onCreateTeam = { name, department, coach, formation ->
                            teamsViewModel.createTeam(
                                name = name,
                                department = department,
                                coach = coach,
                                formation = formation,
                                onSuccess = {
                                    showManageTeamsDialog = false
                                    android.widget.Toast.makeText(context, "Team created successfully", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    )
                }
            }

            composable("${Screen.AdminTeamRoster}/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId") ?: ""
                val team = teams.find { it.id == teamId }
                
                if (team != null) {
                    val players by remember(teamId) { teamsViewModel.getTeamPlayers(teamId) }.collectAsState(initial = emptyList())
                    var showAddPlayerDialog by remember { mutableStateOf(false) }
                    
                    var showAssignLeagueDialog by remember { mutableStateOf(false) }

                    AdminTeamRosterScreen(
                        team = team,
                        players = players,
                        leagues = leagues,
                        onAddPlayerClick = { showAddPlayerDialog = true },
                        onDeletePlayer = { playerId ->
                            teamsViewModel.deletePlayer(playerId)
                        },
                        onDeleteTeam = { teamIdToDelete ->
                            teamsViewModel.deleteTeam(teamIdToDelete)
                            navController.popBackStack()
                            android.widget.Toast.makeText(context, "Team deleted", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onAssignToLeague = { leagueId ->
                            leaguesViewModel.addTeamToLeague(leagueId, team.id, onSuccess = {
                                android.widget.Toast.makeText(context, "Team assigned to league", android.widget.Toast.LENGTH_SHORT).show()
                            }, onError = { error ->
                                android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_SHORT).show()
                            })
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                    
                    if (showAddPlayerDialog) {
                        AddPlayerDialog(
                            onDismiss = { showAddPlayerDialog = false },
                            onAddPlayer = { name, jerseyNumber, position, age ->
                                teamsViewModel.createPlayer(
                                    teamId = teamId,
                                    name = name,
                                    jerseyNumber = jerseyNumber,
                                    position = position,
                                    age = age,
                                    onSuccess = {
                                        showAddPlayerDialog = false
                                        android.widget.Toast.makeText(context, "Player added successfully", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }

            composable("${Screen.MatchDetails}/{matchId}") { backStackEntry ->
                val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                val match = matches.find { it.id == matchId }

                if (match != null) {
                    val events by remember(matchId) { matchesViewModel.getMatchEvents(matchId) }.collectAsState(initial = emptyList())
                    val homePlayers by remember(match.homeTeamId) { teamsViewModel.getTeamPlayers(match.homeTeamId) }.collectAsState(initial = emptyList())
                    val awayPlayers by remember(match.awayTeamId) { teamsViewModel.getTeamPlayers(match.awayTeamId) }.collectAsState(initial = emptyList())
                    
                    MatchDetailsScreen(
                        match = match,
                        events = events,
                        homePlayers = homePlayers,
                        awayPlayers = awayPlayers,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    // Update Score Dialog
    if (showUpdateScoreDialog && selectedMatch != null) {
        UpdateScoreDialog(
            match = selectedMatch!!,
            onDismiss = {
                showUpdateScoreDialog = false
                selectedMatch = null
            },
            onUpdateScore = { homeScore, awayScore ->
                matchesViewModel.updateMatchScore(selectedMatch!!.id, homeScore, awayScore)
                showUpdateScoreDialog = false
                selectedMatch = null
            },
            onUpdateStatus = { status ->
                matchesViewModel.updateMatchStatus(selectedMatch!!.id, status)
            }
        )
    }

    // Add Match Dialog
    if (showAddMatchDialog) {
        AddMatchDialog(
            teams = teams.map { it.name },
            onDismiss = { showAddMatchDialog = false },
            onAddMatch = { homeTeam, awayTeam, venue, round, timestamp, referee ->
                val homeTeamObj = teams.find { it.name == homeTeam }
                val awayTeamObj = teams.find { it.name == awayTeam }
                matchesViewModel.createMatch(
                    homeTeam = homeTeamObj,
                    awayTeam = awayTeamObj,
                    homeTeamName = homeTeam,
                    awayTeamName = awayTeam,
                    venue = venue,
                    round = round,
                    timestampMillis = timestamp,
                    refereeName = referee,
                    onSuccess = {
                        showAddMatchDialog = false
                        Toast.makeText(context, "Match added successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }



    // Manage Leagues Dialog
    if (showManageLeaguesDialog) {
        ManageLeaguesDialog(
            onDismiss = { showManageLeaguesDialog = false },
            onCreateLeague = { name, season, about, rules, prizes, start, end ->
                leaguesViewModel.createLeague(
                    name = name,
                    season = season,
                    about = about,
                    rules = rules,
                    prizes = prizes,
                    startDate = start,
                    endDate = end,
                    onSuccess = {
                        showManageLeaguesDialog = false
                        Toast.makeText(context, "League added successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }


}

@Composable
fun AdminPinDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admin Access") },
        text = {
            Column {
                Text("Enter Admin PIN to continue")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            error = false
                        }
                    },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = error,
                    supportingText = { if (error) Text("Incorrect PIN") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin == "1234") { // Default PIN
                        onSuccess()
                    } else {
                        error = true
                    }
                }
            ) {
                Text("Verify")
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
fun PolyScoresBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem(
                route = Screen.Home,
                icon = Icons.Default.Home,
                label = "Home"
            ),
            BottomNavItem(
                route = Screen.Matches,
                icon = Icons.Default.SportsSoccer,
                label = "Matches"
            ),
            BottomNavItem(
                route = Screen.Standings,
                icon = Icons.Default.Leaderboard,
                label = "Standings"
            ),
            BottomNavItem(
                route = Screen.Teams,
                icon = Icons.Default.People,
                label = "Teams"
            ),
            BottomNavItem(
                route = Screen.Settings,
                icon = Icons.Default.Settings,
                label = "Settings"
            )
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                alwaysShowLabel = true
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

object Screen {
    const val Home = "home"
    const val Matches = "matches"
    const val Standings = "standings"
    const val Teams = "teams"
    const val TeamDetails = "team_details"
    const val Settings = "settings"
    const val Admin = "admin"
    const val AdminManageTeams = "admin_manage_teams"
    const val AdminTeamRoster = "admin_team_roster"
    const val AdminMatchDashboard = "admin_match_dashboard"
    const val AdminManageLeagues = "admin_manage_leagues"
    const val AdminStandingsEditor = "admin_standings_editor"
    const val MatchDetails = "match_details"
}

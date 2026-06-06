package com.polyscores.kenya.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polyscores.kenya.data.model.Player
import com.polyscores.kenya.data.model.PlayerPosition
import com.polyscores.kenya.data.model.MatchEvent
import com.polyscores.kenya.data.model.MatchEventType

@Composable
fun VisualPitchLineup(
    players: List<Player>,
    formation: String,
    events: List<MatchEvent> = emptyList(),
    primaryColor: Color = Color(0xFF1E88E5),
    onPlayerClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Parse the formation string (e.g. "4-3-3" -> [1, 4, 3, 3])
    val rowCounts = parseFormation(formation)
    
    // Sort players by position to attempt correct placement
    val sortedPlayers = players.sortedBy { it.position.ordinal }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f) // pitch aspect ratio
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2E7D32)) // Base green
    ) {
        FootballPitchBackground()
        
        // Players Layer
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            var playerIndex = 0
            
            // Render from bottom to top of the screen (GK at bottom)
            for (count in rowCounts) {
                // Determine layout scaling based on column count in this row
                val sizeMultiplier = when {
                    count >= 5 -> 0.75f
                    count == 4 -> 0.9f
                    else -> 1.0f
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until count) {
                        if (playerIndex < sortedPlayers.size) {
                            val player = sortedPlayers[playerIndex]
                            val isGk = player.position == PlayerPosition.GOALKEEPER
                            
                            PlayerPitchIcon(
                                player = player,
                                primaryColor = primaryColor,
                                isGoalkeeper = isGk,
                                events = events.filter { it.playerId == player.id },
                                sizeMultiplier = sizeMultiplier,
                                onClick = { onPlayerClick(player.id) }
                            )
                            playerIndex++
                        } else {
                            // Empty slot if we ran out of players
                            Box(modifier = Modifier.size((50 * sizeMultiplier).dp))
                        }
                    }
                }
            }
        }
    }
}

private fun parseFormation(formation: String): List<Int> {
    val default = listOf(1, 4, 3, 3)
    if (formation.isBlank()) return default
    
    try {
        val parts = formation.split("-").map { it.toInt() }
        return listOf(1) + parts // 1 for GK, then the rest
    } catch (e: Exception) {
        return default
    }
}

@Composable
fun FootballPitchBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val lineThickness = 2.dp.toPx()
        val lineColor = Color.White.copy(alpha = 0.6f)
        
        // Outer boundary
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(16f, 16f),
            size = Size(width - 32f, height - 32f),
            style = Stroke(width = lineThickness),
            cornerRadius = CornerRadius(4f, 4f)
        )
        
        // Halfway line
        drawLine(
            color = lineColor,
            start = Offset(16f, height / 2),
            end = Offset(width - 16f, height / 2),
            strokeWidth = lineThickness
        )
        
        // Center circle
        drawCircle(
            color = lineColor,
            radius = width * 0.15f,
            center = Offset(width / 2, height / 2),
            style = Stroke(width = lineThickness)
        )
        
        // Center dot
        drawCircle(
            color = lineColor,
            radius = 3.dp.toPx(),
            center = Offset(width / 2, height / 2)
        )
        
        // Top Penalty Area
        drawRect(
            color = lineColor,
            topLeft = Offset(width * 0.25f, 16f),
            size = Size(width * 0.5f, height * 0.15f),
            style = Stroke(width = lineThickness)
        )
        
        // Top Goal Area
        drawRect(
            color = lineColor,
            topLeft = Offset(width * 0.38f, 16f),
            size = Size(width * 0.24f, height * 0.05f),
            style = Stroke(width = lineThickness)
        )
        
        // Top Penalty Arc (D)
        val pathTop = Path().apply {
            addArc(
                androidx.compose.ui.geometry.Rect(
                    left = width * 0.4f,
                    top = 16f + height * 0.1f,
                    right = width * 0.6f,
                    bottom = 16f + height * 0.2f
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(pathTop, color = lineColor, style = Stroke(width = lineThickness))

        // Bottom Penalty Area
        drawRect(
            color = lineColor,
            topLeft = Offset(width * 0.25f, height - 16f - height * 0.15f),
            size = Size(width * 0.5f, height * 0.15f),
            style = Stroke(width = lineThickness)
        )
        
        // Bottom Goal Area
        drawRect(
            color = lineColor,
            topLeft = Offset(width * 0.38f, height - 16f - height * 0.05f),
            size = Size(width * 0.24f, height * 0.05f),
            style = Stroke(width = lineThickness)
        )
        
        // Bottom Penalty Arc (D)
        val pathBottom = Path().apply {
            addArc(
                androidx.compose.ui.geometry.Rect(
                    left = width * 0.4f,
                    top = height - 16f - height * 0.2f,
                    right = width * 0.6f,
                    bottom = height - 16f - height * 0.1f
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f
            )
        }
        drawPath(pathBottom, color = lineColor, style = Stroke(width = lineThickness))
    }
}

@Composable
fun PlayerPitchIcon(
    player: Player,
    primaryColor: Color,
    isGoalkeeper: Boolean,
    events: List<MatchEvent> = emptyList(),
    sizeMultiplier: Float = 1.0f,
    onClick: () -> Unit = {}
) {
    val gkColor = Color(0xFFFDD835) // Yellow for GK
    val jerseyColor = if (isGoalkeeper) gkColor else primaryColor
    val onJerseyColor = if (isGoalkeeper) Color.Black else Color.White
    
    // Check events
    val hasRedCard = events.any { it.eventType == MatchEventType.RED_CARD }
    val hasYellowCard = events.any { it.eventType == MatchEventType.YELLOW_CARD }
    val goalsScored = events.count { it.eventType == MatchEventType.GOAL || it.eventType == MatchEventType.PENALTY_GOAL }
    val isSubbedOut = events.any { it.eventType == MatchEventType.SUBSTITUTION_OUT }
    val isSubbedIn = events.any { it.eventType == MatchEventType.SUBSTITUTION_IN }
    
    // Dim the jersey if player has a red card
    val finalJerseyColor = if (hasRedCard) jerseyColor.copy(alpha = 0.5f) else jerseyColor
    
    // Split name and get last name or shortest readable chunk
    val nameParts = player.name.split(" ")
    val displayName = if (nameParts.size > 1) nameParts.last() else nameParts.first()

    val iconWidth = (60 * sizeMultiplier).dp
    val jerseySize = (32 * sizeMultiplier).dp
    val jerseyBorder = (1.5 * sizeMultiplier).dp
    val jerseyTextSize = (12 * sizeMultiplier).sp
    val nameTextSize = (10 * sizeMultiplier).sp
    val badgeOffset = (8 * sizeMultiplier).dp
    val badgeYOffset = (-4 * sizeMultiplier).dp
    val badgeTextSize = (10 * sizeMultiplier).sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(iconWidth).clickable { onClick() }
    ) {
        // Jersey Circle with Event Badges
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                modifier = Modifier
                    .size(jerseySize)
                    .border(jerseyBorder, if (hasRedCard) Color.Red else Color.White, CircleShape),
                shape = CircleShape,
                color = finalJerseyColor,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (player.jerseyNumber > 0) player.jerseyNumber.toString() else "-",
                        color = onJerseyColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = jerseyTextSize
                    )
                }
            }
            
            // Event Badges
            Row(
                modifier = Modifier.offset(x = badgeOffset, y = badgeYOffset),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hasRedCard) Text("🟥", fontSize = badgeTextSize)
                else if (hasYellowCard) Text("🟨", fontSize = badgeTextSize)
                
                if (goalsScored > 0) {
                    Text(if (goalsScored > 1) "⚽x$goalsScored" else "⚽", fontSize = badgeTextSize)
                }
                
                if (isSubbedOut && !hasRedCard) Text("⬇️", fontSize = badgeTextSize)
                if (isSubbedIn) Text("⬆️", fontSize = badgeTextSize)
            }
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        // Name Label
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = nameTextSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

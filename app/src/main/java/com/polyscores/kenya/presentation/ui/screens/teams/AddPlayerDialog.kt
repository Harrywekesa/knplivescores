package com.polyscores.kenya.presentation.ui.screens.teams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.polyscores.kenya.data.model.PlayerPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onAddPlayer: (String, Int, PlayerPosition, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var jerseyNumberString by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf(PlayerPosition.FORWARD) }
    var expandedPosition by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Player") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = jerseyNumberString,
                        onValueChange = { jerseyNumberString = it.filter { char -> char.isDigit() } },
                        label = { Text("Jersey #") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = ageString,
                        onValueChange = { ageString = it.filter { char -> char.isDigit() } },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = expandedPosition,
                    onExpandedChange = { expandedPosition = it }
                ) {
                    OutlinedTextField(
                        value = selectedPosition.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Position") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPosition) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPosition,
                        onDismissRequest = { expandedPosition = false }
                    ) {
                        PlayerPosition.values().forEach { position ->
                            DropdownMenuItem(
                                text = { Text(position.name) },
                                onClick = {
                                    selectedPosition = position
                                    expandedPosition = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val jerseyNumber = jerseyNumberString.toIntOrNull() ?: 0
                    val age = ageString.toIntOrNull() ?: 0
                    onAddPlayer(name, jerseyNumber, selectedPosition, age)
                    onDismiss()
                },
                enabled = name.isNotBlank() && jerseyNumberString.isNotBlank()
            ) {
                Text("Add Player")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

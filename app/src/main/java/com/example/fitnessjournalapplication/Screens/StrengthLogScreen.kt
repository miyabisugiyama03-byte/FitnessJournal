package com.example.fitnessjournalapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnessjournalapplication.data.StrengthExercise
import com.example.fitnessjournalapplication.data.StrengthExerciseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthLogScreen(
    dao: StrengthExerciseDao,
    onBack: () -> Unit
) {
    val today = LocalDate.now()
    var showDialog by remember { mutableStateOf(false) }
    var newExercise by remember { mutableStateOf("") }
    var newSets by remember { mutableStateOf("") }
    var newReps by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }

    val exercises by dao.getExercisesForDate(today).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Strength Training Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Favorite, contentDescription = "Add Exercise")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (exercises.isEmpty()) {
                item {
                    Text(
                        "No exercises logged for today",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(exercises.size) { index ->
                    val exercise = exercises[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(exercise.exercise, style = MaterialTheme.typography.titleMedium)
                            Text("Sets: ${exercise.sets}, Reps: ${exercise.reps}, Weight: ${exercise.weight} kg")
                        }
                    }
                }
            }
        }
    }


    // Dialog to add new strength exercise
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Strength Exercise") },
            text = {
                Column {
                    // Exercise name
                    TextField(
                        value = newExercise,
                        onValueChange = { newExercise = it },
                        placeholder = { Text("Exercise name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Sets, Reps, Weight
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = newSets,
                            onValueChange = { newSets = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("Sets") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = newReps,
                            onValueChange = { newReps = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("Reps") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = newWeight,
                            onValueChange = {
                                newWeight = it.filter { char -> char.isDigit() || char == '.' }
                            },
                            placeholder = { Text("Weight") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Make sure exercise name is not blank
                    if (newExercise.isNotBlank()) {
                        val setsValue = newSets.toIntOrNull() ?: 0
                        val repsValue = newReps.toIntOrNull() ?: 0
                        val weightValue = newWeight.toFloatOrNull() ?: 0f

                        // Insert into Room
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.insert(
                                StrengthExercise(
                                    date = today,
                                    exercise = newExercise,
                                    sets = setsValue,
                                    reps = repsValue,
                                    weight = weightValue
                                )
                            )
                        }

                        // Reset input fields
                        newExercise = ""
                        newSets = ""
                        newReps = ""
                        newWeight = ""
                        showDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

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
import com.example.fitnessjournalapplication.data.CardioExercise
import com.example.fitnessjournalapplication.data.CardioExerciseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioLogScreen(
    dao: CardioExerciseDao,
    onBack: () -> Unit
) {
    val today = LocalDate.now()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CardioExercise?>(null) }

    val exercises by dao.getExercisesForDate(today).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cardio Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                item { Text("No cardio exercises logged for today", style = MaterialTheme.typography.bodyLarge) }
            } else {
                items(exercises.size) { index ->
                    val exercise = exercises[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(exercise.exercise, style = MaterialTheme.typography.titleMedium)
                            Text("Duration: ${exercise.duration} mins, Distance: ${exercise.distance} km")

                            Spacer(Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showEditDialog = exercise }) { Text("Edit") }
                                Button(onClick = {
                                    CoroutineScope(Dispatchers.IO).launch { dao.delete(exercise) }
                                }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------- ADD DIALOG -------------------
    if (showAddDialog) {
        CardioExerciseDialog(
            title = "Add Cardio Exercise",
            initialExercise = null,
            onDismiss = { showAddDialog = false },
            onSave = { exercise ->
                CoroutineScope(Dispatchers.IO).launch { dao.insert(exercise) }
                showAddDialog = false
            }
        )
    }

    // ------------------- EDIT DIALOG -------------------
    showEditDialog?.let { exercise ->
        CardioExerciseDialog(
            title = "Edit Cardio Exercise",
            initialExercise = exercise,
            onDismiss = { showEditDialog = null },
            onSave = { updatedExercise ->
                CoroutineScope(Dispatchers.IO).launch { dao.update(updatedExercise) }
                showEditDialog = null
            }
        )
    }
}

@Composable
fun CardioExerciseDialog(
    title: String,
    initialExercise: CardioExercise?,
    onDismiss: () -> Unit,
    onSave: (CardioExercise) -> Unit
) {
    var name by remember { mutableStateOf(initialExercise?.exercise ?: "") }
    var duration by remember { mutableStateOf(initialExercise?.duration?.toString() ?: "") }
    var distance by remember { mutableStateOf(initialExercise?.distance?.toString() ?: "") }
    val date = initialExercise?.date ?: LocalDate.now()
    val id = initialExercise?.id ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Exercise name") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = duration,
                        onValueChange = { duration = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("Duration (mins)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextField(
                        value = distance,
                        onValueChange = { distance = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = { Text("Distance (km)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val exercise = CardioExercise(
                        id = id,
                        date = date,
                        exercise = name,
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toFloatOrNull() ?: 0f
                    )
                    onSave(exercise)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

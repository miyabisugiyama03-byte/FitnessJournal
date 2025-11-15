package com.example.fitnessjournalapplication.ui.screens

import androidx.compose.foundation.clickable
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
    selectedDate: LocalDate,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<CardioExercise?>(null) }

    val exercises by dao.getExercisesForDate(selectedDate).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cardio Log - ${selectedDate.toString()}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingExercise = null
                showDialog = true
            }) {
                Icon(Icons.Default.Favorite, contentDescription = "Add Cardio")
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
                item { Text("No cardio exercises logged for $selectedDate", style = MaterialTheme.typography.bodyLarge) }
            } else {
                items(exercises.size) { index ->
                    val ex = exercises[index]
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingExercise = ex
                            showDialog = true
                        }, elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ex.exercise, style = MaterialTheme.typography.titleMedium)
                            Text("Duration: ${ex.duration} min, Distance: ${ex.distance} km")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { editingExercise = ex }) { Text("Edit") }
                                TextButton(onClick = { CoroutineScope(Dispatchers.IO).launch { dao.delete(ex) } }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CardioExerciseDialogShared(
            title = if (editingExercise == null) "Add Cardio" else "Edit Cardio",
            initial = editingExercise,
            date = selectedDate,
            onDismiss = { showDialog = false; editingExercise = null },
            onSave = { cardio ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (editingExercise == null) dao.insert(cardio) else dao.update(cardio)
                }
                editingExercise = null
                showDialog = false
            }
        )
    }
}

@Composable
fun CardioExerciseDialogShared(
    title: String,
    initial: CardioExercise?,
    date: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CardioExercise) -> Unit
) {
    var name by remember { mutableStateOf(initial?.exercise ?: "") }
    var duration by remember { mutableStateOf(initial?.duration?.toString() ?: "") }
    var distance by remember { mutableStateOf(initial?.distance?.toString() ?: "") }
    val id = initial?.id ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, placeholder = { Text("Exercise name") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(value = duration, onValueChange = { duration = it.filter { c -> c.isDigit() } }, placeholder = { Text("Duration (min)") }, modifier = Modifier.weight(1f), singleLine = true)
                    TextField(value = distance, onValueChange = { distance = it.filter { c -> c.isDigit() || c == '.' } }, placeholder = { Text("Distance (km)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val cardio = CardioExercise(
                        id = id,
                        date = date,
                        exercise = name,
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toFloatOrNull() ?: 0f
                    )
                    onSave(cardio)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

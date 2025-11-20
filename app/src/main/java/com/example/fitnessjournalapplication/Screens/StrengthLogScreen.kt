@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnessjournalapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnessjournalapplication.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun StrengthLogScreen(
    dao: StrengthExerciseDao,
    masterDao: StrengthMasterExerciseDao,
    selectedDate: LocalDate,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<StrengthExercise?>(null) }
    var showMasterDialog by remember { mutableStateOf(false) }

    val exercises by dao.getExercisesForDate(selectedDate).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Strength Log - $selectedDate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMasterDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Master Exercises")
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
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (exercises.isEmpty()) {
                item { Text("No exercises logged for $selectedDate") }
            } else {
                items(exercises.size) { index ->
                    val ex = exercises[index]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(ex.exercise, style = MaterialTheme.typography.titleMedium)
                            Text("Sets: ${ex.sets}, Reps: ${ex.reps}, Weight: ${ex.weight} kg")

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { showEditDialog = ex }) { Text("Edit") }
                                TextButton(onClick = {
                                    scope.launch(Dispatchers.IO) { dao.delete(ex) }
                                }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add entry dialog
    if (showAddDialog) {
        StrengthExerciseDialog(
            title = "Add Exercise",
            masterDao = masterDao,
            initial = null,
            selectedDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onSave = {
                scope.launch(Dispatchers.IO) { dao.insert(it) }
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    showEditDialog?.let { exercise ->
        StrengthExerciseDialog(
            title = "Edit Exercise",
            masterDao = masterDao,
            initial = exercise,
            selectedDate = selectedDate,
            onDismiss = { showEditDialog = null },
            onSave = {
                scope.launch(Dispatchers.IO) { dao.update(it) }
                showEditDialog = null
            }
        )
    }

    // Master list dialog
    if (showMasterDialog) {
        MasterExerciseDialog(
            masterDao = masterDao,
            onDismiss = { showMasterDialog = false }
        )
    }
}

/* ---------------- StrengthExerciseDialog ---------------- */

@Composable
fun StrengthExerciseDialog(
    title: String,
    masterDao: StrengthMasterExerciseDao,
    initial: StrengthExercise?,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (StrengthExercise) -> Unit
) {
    val scope = rememberCoroutineScope()
    val masterExercises by masterDao.getAllExercises().collectAsState(initial = emptyList())

    var selectedExercise by remember { mutableStateOf(initial?.exercise ?: "") }
    var sets by remember { mutableStateOf(initial?.sets?.toString() ?: "") }
    var reps by remember { mutableStateOf(initial?.reps?.toString() ?: "") }
    var weight by remember { mutableStateOf(initial?.weight?.toString() ?: "") }

    // separate states for dropdown vs "add new master" dialog
    var showDropdown by remember { mutableStateOf(false) }
    var showAddMasterDialog by remember { mutableStateOf(false) }
    var newMasterExercise by remember { mutableStateOf("") }

    val id = initial?.id ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Exercise dropdown (shows master list + option to add)
                ExposedDropdownMenuBox(
                    expanded = showDropdown,
                    onExpandedChange = { showDropdown = it }
                ) {
                    TextField(
                        value = selectedExercise,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exercise") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        // show available master exercises
                        if (masterExercises.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No master exercises yet") },
                                onClick = { /* no-op */ }
                            )
                        } else {
                            masterExercises.forEach { ex ->
                                DropdownMenuItem(
                                    text = { Text(ex.name) },
                                    onClick = {
                                        selectedExercise = ex.name
                                        showDropdown = false
                                    }
                                )
                            }
                        }

                        // Add new master exercise option
                        DropdownMenuItem(
                            text = { Text("➕ Add New Exercise…") },
                            onClick = {
                                showDropdown = false
                                showAddMasterDialog = true
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row {
                    TextField(
                        value = sets,
                        onValueChange = { sets = it.filter(Char::isDigit) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Sets") }
                    )
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = reps,
                        onValueChange = { reps = it.filter(Char::isDigit) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Reps") }
                    )
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = weight,
                        onValueChange = { weight = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        modifier = Modifier.weight(1f),
                        label = { Text("Weight (kg)") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedExercise.isNotBlank()) {
                    onSave(
                        StrengthExercise(
                            id = id,
                            date = selectedDate,
                            exercise = selectedExercise,
                            sets = sets.toIntOrNull() ?: 0,
                            reps = reps.toIntOrNull() ?: 0,
                            weight = weight.toFloatOrNull() ?: 0f
                        )
                    )
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    // Add New Master Exercise dialog (simple text input)
    if (showAddMasterDialog) {
        AlertDialog(
            onDismissRequest = { showAddMasterDialog = false },
            title = { Text("Add New Exercise") },
            text = {
                Column {
                    TextField(
                        value = newMasterExercise,
                        onValueChange = { newMasterExercise = it },
                        placeholder = { Text("Exercise name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newMasterExercise.trim()
                    if (name.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            masterDao.insert(StrengthMasterExercise(name = name))
                        }
                        // set the newly added exercise as the selected one immediately
                        selectedExercise = name
                    }
                    newMasterExercise = ""
                    showAddMasterDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMasterDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/* ---------------- MasterExerciseDialog ---------------- */

@Composable
fun MasterExerciseDialog(
    masterDao: StrengthMasterExerciseDao,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var newExercise by remember { mutableStateOf("") }
    val exercises by masterDao.getAllExercises().collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Master Exercise List") },
        text = {
            Column {
                if (exercises.isEmpty()) {
                    Text("No master exercises yet")
                } else {
                    exercises.forEach { ex ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ex.name)
                            TextButton(onClick = {
                                scope.launch(Dispatchers.IO) { masterDao.delete(ex) }
                            }) { Text("Delete") }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextField(
                    value = newExercise,
                    onValueChange = { newExercise = it },
                    label = { Text("Add Exercise") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val name = newExercise.trim()
                if (name.isNotBlank()) {
                    scope.launch(Dispatchers.IO) { masterDao.insert(StrengthMasterExercise(name = name)) }
                }
                newExercise = ""
                onDismiss()
            }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

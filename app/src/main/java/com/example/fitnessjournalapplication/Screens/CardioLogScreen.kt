@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnessjournalapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnessjournalapplication.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/* ---------------------------------------------------------
   CARDIO LOG SCREEN
--------------------------------------------------------- */

@Composable
fun CardioLogScreen(
    dao: CardioExerciseDao,
    masterDao: CardioMasterExerciseDao,
    selectedDate: LocalDate,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<CardioExercise?>(null) }
    var showMasterDialog by remember { mutableStateOf(false) }

    val exercises by dao.getExercisesForDate(selectedDate).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cardio Log - $selectedDate") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMasterDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Master Cardio List")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Favorite, contentDescription = "Add Cardio")
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
                item { Text("No cardio logged for $selectedDate") }
            } else {
                items(exercises.size) { index ->
                    val ex = exercises[index]
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(ex.exercise, style = MaterialTheme.typography.titleMedium)
                            Text("Distance: ${ex.distance} km")
                            Text("Duration: ${ex.duration} min")


                            Spacer(Modifier.height(8.dp))

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

    // Add dialog
    if (showAddDialog) {
        CardioExerciseDialog(
            title = "Add Cardio Exercise",
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
        CardioExerciseDialog(
            title = "Edit Cardio Exercise",
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

    if (showMasterDialog) {
        CardioMasterDialog(
            masterDao = masterDao,
            onDismiss = { showMasterDialog = false }
        )
    }
}

/* ---------------------------------------------------------
   CARDIO EXERCISE DIALOG
--------------------------------------------------------- */

@Composable
fun CardioExerciseDialog(
    title: String,
    masterDao: CardioMasterExerciseDao,
    initial: CardioExercise?,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (CardioExercise) -> Unit
) {
    val scope = rememberCoroutineScope()
    val masterExercises by masterDao.getAllExercises().collectAsState(initial = emptyList())

    var selectedExercise by remember { mutableStateOf(initial?.exercise ?: "") }
    var distance by remember { mutableStateOf(initial?.distance?.toString() ?: "") }
    var duration by remember { mutableStateOf(initial?.duration?.toString() ?: "") }


    var showDropdown by remember { mutableStateOf(false) }
    var showAddMasterDialog by remember { mutableStateOf(false) }
    var newMasterExercise by remember { mutableStateOf("") }

    val id = initial?.id ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
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
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        masterExercises.forEach { ex ->
                            DropdownMenuItem(
                                text = { Text(ex.name) },
                                onClick = {
                                    selectedExercise = ex.name
                                    showDropdown = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("➕ Add New Exercise…") },
                            onClick = {
                                showAddMasterDialog = true
                                showDropdown = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                TextField(
                    value = distance,
                    onValueChange = { distance = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Distance (km)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = duration,
                    onValueChange = { duration = it.filter(Char::isDigit) },
                    label = { Text("Duration (min)") },
                    modifier = Modifier.fillMaxWidth()
                )


            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedExercise.isNotBlank()) {
                    onSave(
                        CardioExercise(
                            id = id,
                            date = selectedDate,
                            exercise = selectedExercise,
                            distance = distance.toFloatOrNull() ?: 0f,
                            duration = duration.toIntOrNull() ?: 0,

                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    // Add New Master Cardio Exercise
    if (showAddMasterDialog) {
        AlertDialog(
            onDismissRequest = { showAddMasterDialog = false },
            title = { Text("Add New Cardio Exercise") },
            text = {
                TextField(
                    value = newMasterExercise,
                    onValueChange = { newMasterExercise = it },
                    label = { Text("Exercise Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = newMasterExercise.trim()
                    if (name.isNotBlank()) {
                        scope.launch(Dispatchers.IO) {
                            masterDao.insert(CardioMasterExercise(name = name))
                        }
                        selectedExercise = name
                    }
                    showAddMasterDialog = false
                    newMasterExercise = ""
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddMasterDialog = false }) { Text("Cancel") } }
        )
    }
}

/* ---------------------------------------------------------
   CARDIO MASTER LIST DIALOG
--------------------------------------------------------- */

@Composable
fun CardioMasterDialog(
    masterDao: CardioMasterExerciseDao,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var newExercise by remember { mutableStateOf("") }
    val exercises by masterDao.getAllExercises().collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Master Cardio List") },
        text = {
            Column {
                exercises.forEach { ex ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ex.name)
                        TextButton(onClick = {
                            scope.launch(Dispatchers.IO) { masterDao.delete(ex) }
                        }) { Text("Delete") }
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextField(
                    value = newExercise,
                    onValueChange = { newExercise = it },
                    label = { Text("Add Cardio Exercise") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val name = newExercise.trim()
                if (name.isNotBlank()) {
                    scope.launch(Dispatchers.IO) {
                        masterDao.insert(CardioMasterExercise(name = name))
                    }
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

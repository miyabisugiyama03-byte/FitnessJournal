package com.example.fitnessjournalapplication.Screens


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnessjournalapplication.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    type: String, // "strength" or "cardio"
    id: Int,
    strengthDao: StrengthExerciseDao,
    cardioDao: CardioExerciseDao,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    // Load existing notes
    LaunchedEffect(id) {
        if (type == "strength") {
            val item = strengthDao.getExerciseById(id)
            title = item?.exercise ?: "Strength Exercise"
            text = item?.notes ?: ""
        } else {
            val item = cardioDao.getExerciseById(id)
            title = item?.exercise ?: "Cardio Exercise"
            text = item?.notes ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notes - $title") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Text("Workout Notes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Enter notes about this workout…") },
                maxLines = Int.MAX_VALUE
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        if (type == "strength") {
                            val ex = strengthDao.getExerciseById(id)
                            if (ex != null) {
                                strengthDao.update(ex.copy(notes = text))
                            }
                        } else {
                            val ex = cardioDao.getExerciseById(id)
                            if (ex != null) {
                                cardioDao.update(ex.copy(notes = text))
                            }
                        }
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Notes")
            }
        }
    }
}

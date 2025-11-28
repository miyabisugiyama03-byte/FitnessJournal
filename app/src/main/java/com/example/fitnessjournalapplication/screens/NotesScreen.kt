package com.example.fitnessjournalapplication.screens


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
    type: String, // identifies whether we are editing a strength or cardio entry
    id: Int,      // exercise record ID
    strengthDao: StrengthExerciseDao,
    cardioDao: CardioExerciseDao,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    /*
     Load existing notes for the specific log item.
     Using LaunchedEffect ensures the lookup only runs once
     when the ID changes, and avoids doing DB access inside UI.
   */
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
                // Dynamically shows the exercise name in the header
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



            //Notes entry field

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

            /*
             Save button writes notes back to the correct DAO.
             Runs in IO dispatcher to avoid blocking the UI thread.
           */

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

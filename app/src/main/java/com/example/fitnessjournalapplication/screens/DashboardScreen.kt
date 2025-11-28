@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnessjournalapplication.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fitnessjournalapplication.data.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen() {

    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)

    // Pull DAOs from the database once to avoid recomputing references.
    val strengthDao = db.strengthExerciseDao()
    val strengthMasterDao = db.strengthMasterExerciseDao()
    val cardioDao = db.cardioExerciseDao()
    val cardioMasterDao = db.cardioMasterExerciseDao()
    val weeklyGoalDao = db.weeklyGoalDao()

    val scope = rememberCoroutineScope()

    // Collect all required data streams from DB.
    // Dashboard must react live as logs/goals change.
    val strengthMasterList by strengthMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val strengthLogs by strengthDao.getAllStrengthExercises().collectAsState(initial = emptyList())
    val cardioMasterList by cardioMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val cardioLogs by cardioDao.getAllCardioExercises().collectAsState(initial = emptyList())

    // Weekly goal defaults to 3 if unset (helps first-time users).
    val weeklyGoalState by weeklyGoalDao.getWeeklyGoal().collectAsState(initial = null)
    val currentGoal = weeklyGoalState?.goalPerWeek ?: 3
    // Keep the text field UI in sync with stored goal.
    var goalInput by remember { mutableStateOf(currentGoal.toString()) }

    // Weekly workout calculation (Mon–Sun).
    // Any day with *at least one* log counts as one workout.
    val today = LocalDate.now()
    val start = today.with(DayOfWeek.MONDAY)
    val end = start.plusDays(6)

    val wStrength = strengthLogs.filter { it.date in start..end }
    val wCardio = cardioLogs.filter { it.date in start..end }
    // Unique days = number of workouts.

    val workoutsThisWeek = (wStrength.map { it.date } + wCardio.map { it.date }).toSet().size
    // Prevent crashing if goal is 0.
    val progress = (workoutsThisWeek.toFloat() / currentGoal.toFloat()).coerceIn(0f, 1f)

    // SELECTED TAB: "strength" or "cardio"
    var selectedTab by remember { mutableStateOf("strength") }

    // Dropdown selections
    var selectedStrength by remember { mutableStateOf("") }
    var strengthDropdownOpen by remember { mutableStateOf(false) }

    var selectedCardio by remember { mutableStateOf("") }
    var cardioDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Dashboard", style = MaterialTheme.typography.headlineLarge)
        Text("Track your weekly goals & workout progress")

        Spacer(Modifier.height(16.dp))

        // WEEKLY GOAL CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Weekly Goal", style = MaterialTheme.typography.titleMedium)
                Text("$workoutsThisWeek / $currentGoal workouts")

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Users can set their own goal; validate via digit filtering.
                    TextField(
                        value = goalInput,
                        onValueChange = { goalInput = it.filter(Char::isDigit) },
                        label = { Text("Goal per week") },
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = {
                        // Only update DB with valid positive goals.
                        val newGoal = goalInput.toIntOrNull()?.takeIf { it > 0 } ?: currentGoal
                        scope.launch(Dispatchers.IO) {
                            weeklyGoalDao.upsert(WeeklyGoal(id = 0, goalPerWeek = newGoal))
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        //TOGGLE BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = selectedTab == "strength",
                onClick = { selectedTab = "strength" },
                label = { Text("Strength") }
            )
            Spacer(Modifier.width(12.dp))
            FilterChip(
                selected = selectedTab == "cardio",
                onClick = { selectedTab = "cardio" },
                label = { Text("Cardio") }
            )
        }

        Spacer(Modifier.height(20.dp))


        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(200))
            }
        ) { tab ->
            when (tab) {
                "strength" -> StrengthChart(
                    masterList = strengthMasterList,
                    logs = strengthLogs,
                    selected = selectedStrength,
                    onSelectedChange = { selectedStrength = it },
                    dropdownOpen = strengthDropdownOpen,
                    onDropdownChange = { strengthDropdownOpen = it }
                )
                "cardio" -> CardioChart(
                    masterList = cardioMasterList,
                    logs = cardioLogs,
                    selected = selectedCardio,
                    onSelectedChange = { selectedCardio = it },
                    dropdownOpen = cardioDropdownOpen,
                    onDropdownChange = { cardioDropdownOpen = it }
                )
            }
        }
    }
}



@Composable
fun StrengthChart(
    masterList: List<StrengthMasterExercise>,
    logs: List<StrengthExercise>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    dropdownOpen: Boolean,
    onDropdownChange: (Boolean) -> Unit
) {

    /* Gradient Background */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                )
            )
            .padding(16.dp)
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            /* Dropdown */
            ExposedDropdownMenuBox(
                expanded = dropdownOpen,
                onExpandedChange = onDropdownChange
            ) {
                TextField(
                    value = selected,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Exercise") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) }
                )

                ExposedDropdownMenu(
                    expanded = dropdownOpen,
                    onDismissRequest = { onDropdownChange(false) }
                ) {
                    masterList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                onSelectedChange(item.name)
                                onDropdownChange(false)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val filtered = logs.filter { it.exercise == selected }.sortedBy { it.date }
            val entries = filtered.mapIndexed { index, log ->
                Entry(index.toFloat(), log.weight)
            }

            if (selected.isBlank()) {
                Text("Choose an exercise to view progress", color = Color.White)
            } else if (entries.isEmpty()) {
                Text("No data for $selected yet", color = Color.White)
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    factory = { ctx -> LineChart(ctx) },
                    update = { chart ->
                        val ds = LineDataSet(entries, "$selected (kg)").apply {
                            lineWidth = 3f
                            setDrawCircles(true)
                            setDrawValues(false)
                            color = android.graphics.Color.WHITE
                            setCircleColor(android.graphics.Color.WHITE)
                        }
                        chart.data = LineData(ds)
                        chart.xAxis.valueFormatter =
                            IndexAxisValueFormatter(filtered.map { it.date.toString() })
                        chart.axisRight.isEnabled = false
                        chart.description.isEnabled = false
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

@Composable
fun CardioChart(
    masterList: List<CardioMasterExercise>,
    logs: List<CardioExercise>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    dropdownOpen: Boolean,
    onDropdownChange: (Boolean) -> Unit
) {

    /* Gradient */
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F9B8E), Color(0xFF0BC2A0))
                )
            )
            .padding(16.dp)
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            ExposedDropdownMenuBox(
                expanded = dropdownOpen,
                onExpandedChange = onDropdownChange
            ) {
                TextField(
                    value = selected,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Exercise") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) }
                )

                ExposedDropdownMenu(
                    expanded = dropdownOpen,
                    onDismissRequest = { onDropdownChange(false) }
                ) {
                    masterList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                onSelectedChange(item.name)
                                onDropdownChange(false)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val filtered = logs.filter { it.exercise == selected }.sortedBy { it.date }
            val entries = filtered.mapIndexed { index, log ->
                Entry(index.toFloat(), log.distance)
            }

            if (selected.isBlank()) {
                Text("Choose a cardio exercise", color = Color.White)
            } else if (entries.isEmpty()) {
                Text("No data yet for $selected", color = Color.White)
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    factory = { ctx -> LineChart(ctx) },
                    update = { chart ->
                        val ds = LineDataSet(entries, "$selected (km)").apply {
                            lineWidth = 3f
                            setDrawCircles(true)
                            setDrawValues(false)
                            color = android.graphics.Color.WHITE
                            setCircleColor(android.graphics.Color.WHITE)
                        }
                        chart.data = LineData(ds)
                        chart.xAxis.valueFormatter =
                            IndexAxisValueFormatter(filtered.map { it.date.toString() })
                        chart.axisRight.isEnabled = false
                        chart.description.isEnabled = false
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

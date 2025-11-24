package com.example.fitnessjournalapplication.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {

    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)

    val strengthDao = db.strengthExerciseDao()
    val strengthMasterDao = db.strengthMasterExerciseDao()
    val cardioDao = db.cardioExerciseDao()
    val cardioMasterDao = db.cardioMasterExerciseDao()
    val weeklyGoalDao = db.weeklyGoalDao()

    val scope = rememberCoroutineScope()

    // ---------- Data for charts ----------
    val strengthMasterList by strengthMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val strengthLogs by strengthDao.getAllStrengthExercises().collectAsState(initial = emptyList())

    val cardioMasterList by cardioMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val cardioLogs by cardioDao.getAllCardioExercises().collectAsState(initial = emptyList())

    // ---------- Weekly goal from DB ----------
    val weeklyGoalState by weeklyGoalDao.getWeeklyGoal().collectAsState(initial = null)
    val currentGoal = weeklyGoalState?.goalPerWeek ?: 3

    var goalInput by remember { mutableStateOf(currentGoal.toString()) }

    // ---------- Calculate current week workouts ----------
    val today = LocalDate.now()
    val startOfWeek = today.with(DayOfWeek.MONDAY)
    val endOfWeek = startOfWeek.plusDays(6)

    val weekStrengthLogs = strengthLogs.filter { it.date in startOfWeek..endOfWeek }
    val weekCardioLogs = cardioLogs.filter { it.date in startOfWeek..endOfWeek }

    // each day with ANY workout counts once
    val workoutsThisWeek = (weekStrengthLogs.map { it.date } + weekCardioLogs.map { it.date })
        .toSet()
        .size

    val goalInt = currentGoal.coerceAtLeast(1)
    val progress = (workoutsThisWeek.toFloat() / goalInt.toFloat()).coerceIn(0f, 1f)

    // ---------- Chart selection state ----------
    var selectedStrength by remember { mutableStateOf("") }
    var strengthDropdownOpen by remember { mutableStateOf(false) }

    var selectedCardio by remember { mutableStateOf("") }
    var cardioDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Workout Progress Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        /* ============================================================
                 WEEKLY GOAL PROGRESS
        ============================================================ */

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Weekly Workout Goal", style = MaterialTheme.typography.titleMedium)
                Text("This week: $workoutsThisWeek / $goalInt workouts")

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = goalInput,
                        onValueChange = { text ->
                            goalInput = text.filter { it.isDigit() }
                        },
                        label = { Text("Goal per week") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            val newGoal = goalInput.toIntOrNull()?.takeIf { it > 0 } ?: currentGoal
                            goalInput = newGoal.toString()
                            scope.launch(Dispatchers.IO) {
                                weeklyGoalDao.upsert(WeeklyGoal(id = 0, goalPerWeek = newGoal))
                            }
                        }
                    ) {
                        Text("Update")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ============================================================
                 STRENGTH GRAPH
        ============================================================ */

        Text("Strength Progress", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(
            expanded = strengthDropdownOpen,
            onExpandedChange = { strengthDropdownOpen = it }
        ) {
            TextField(
                value = selectedStrength,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Strength Exercise") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = strengthDropdownOpen) }
            )

            ExposedDropdownMenu(
                expanded = strengthDropdownOpen,
                onDismissRequest = { strengthDropdownOpen = false }
            ) {
                strengthMasterList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            selectedStrength = item.name
                            strengthDropdownOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val strengthFiltered = strengthLogs
            .filter { it.exercise == selectedStrength }
            .sortedBy { it.date }

        val strengthEntries = strengthFiltered.mapIndexed { index, log ->
            Entry(index.toFloat(), log.weight)
        }

        if (selectedStrength.isBlank()) {
            Text("Select a strength exercise to view progress")
        } else if (strengthEntries.isEmpty()) {
            Text("No data logged for $selectedStrength yet")
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { ctx -> LineChart(ctx) },
                update = { chart ->
                    val dataSet = LineDataSet(strengthEntries, "$selectedStrength Weight (kg)").apply {
                        lineWidth = 3f
                        setDrawCircles(true)
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter =
                        IndexAxisValueFormatter(strengthFiltered.map { it.date.toString() })
                    chart.xAxis.granularity = 1f
                    chart.axisLeft.axisMinimum = 0f
                    chart.axisRight.isEnabled = false
                    chart.description.text = "Date"
                    chart.invalidate()
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        /* ============================================================
                 CARDIO GRAPH
        ============================================================ */

        Text("Cardio Progress", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(
            expanded = cardioDropdownOpen,
            onExpandedChange = { cardioDropdownOpen = it }
        ) {
            TextField(
                value = selectedCardio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Cardio Exercise") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cardioDropdownOpen) }
            )

            ExposedDropdownMenu(
                expanded = cardioDropdownOpen,
                onDismissRequest = { cardioDropdownOpen = false }
            ) {
                cardioMasterList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.name) },
                        onClick = {
                            selectedCardio = item.name
                            cardioDropdownOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val cardioFiltered = cardioLogs
            .filter { it.exercise == selectedCardio }
            .sortedBy { it.date }

        val cardioEntries = cardioFiltered.mapIndexed { index, log ->
            Entry(index.toFloat(), log.distance)
        }

        if (selectedCardio.isBlank()) {
            Text("Select a cardio exercise to view progress")
        } else if (cardioEntries.isEmpty()) {
            Text("No data logged for $selectedCardio yet")
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { ctx -> LineChart(ctx) },
                update = { chart ->
                    val dataSet = LineDataSet(cardioEntries, "$selectedCardio Distance (km)").apply {
                        lineWidth = 3f
                        setDrawCircles(true)
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.valueFormatter =
                        IndexAxisValueFormatter(cardioFiltered.map { it.date.toString() })
                    chart.xAxis.granularity = 1f
                    chart.axisLeft.axisMinimum = 0f
                    chart.axisRight.isEnabled = false
                    chart.description.text = "Date"
                    chart.invalidate()
                }
            )
        }
    }
}

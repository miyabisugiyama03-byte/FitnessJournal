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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {

    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)

    val strengthDao = db.strengthExerciseDao()
    val strengthMasterDao = db.strengthMasterExerciseDao()
    val cardioDao = db.cardioExerciseDao()
    val cardioMasterDao = db.cardioMasterExerciseDao()

    /* -------- Load all data -------- */

    val strengthMasterList by strengthMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val strengthLogs by strengthDao.getAllStrengthExercises().collectAsState(initial = emptyList())

    val cardioMasterList by cardioMasterDao.getAllExercises().collectAsState(initial = emptyList())
    val cardioLogs by cardioDao.getAllCardioExercises().collectAsState(initial = emptyList())

    /* -------- Dropdown states -------- */

    var selectedStrength by remember { mutableStateOf("") }
    var strengthDropdownOpen by remember { mutableStateOf(false) }

    var selectedCardio by remember { mutableStateOf("") }
    var cardioDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Workout Progress Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        /* ============================================================
                        STRENGTH GRAPH SECTION
        ============================================================ */
        Text("Strength Progress", style = MaterialTheme.typography.titleLarge)

        // ---------------- Dropdown ----------------
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

        /* Filter data for graph */
        val strengthFiltered = strengthLogs
            .filter { it.exercise == selectedStrength }
            .sortedBy { it.date }

        val strengthEntries = strengthFiltered.mapIndexed { index, log ->
            Entry(index.toFloat(), log.weight)
        }

        if (selectedStrength.isBlank()) {
            Text("Select an exercise to view progress")
        } else if (strengthEntries.isEmpty()) {
            Text("No data logged for $selectedStrength yet")
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { ctx ->
                    LineChart(ctx)
                },
                update = { chart ->

                    val dataSet = LineDataSet(strengthEntries, "$selectedStrength Weight (kg)").apply {
                        lineWidth = 3f
                        setDrawCircles(true)
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)

                    // Axis labels
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

        Spacer(Modifier.height(40.dp))

        /* ============================================================
                        CARDIO GRAPH SECTION
        ============================================================ */
        Text("Cardio Progress", style = MaterialTheme.typography.titleLarge)

        // ---------------- Dropdown ----------------
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

        /* Filter cardio logs */
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
                factory = { ctx ->
                    LineChart(ctx)
                },
                update = { chart ->

                    val dataSet = LineDataSet(cardioEntries, "$selectedCardio Distance (km)").apply {
                        lineWidth = 3f
                        setDrawCircles(true)
                        setDrawValues(false)
                    }

                    chart.data = LineData(dataSet)

                    // Axis labels
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

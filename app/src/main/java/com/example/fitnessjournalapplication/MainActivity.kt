@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnessjournalapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.fitnessjournalapplication.ui.theme.FitnessJournalApplicationTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.time.*
import com.example.fitnessjournalapplication.data.* // AppDatabase and DAO
import com.example.fitnessjournalapplication.ui.screens.StrengthLogScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessJournalApplicationTheme {
                FitnessJournalApp()
            }
        }
    }
}

object Routes {
    const val DASHBOARD = "dashboard"
    const val EXERCISE_LOG = "exercise_log"
    const val STRENGTH_LOG = "strength_log"
    const val CARDIO_LOG = "cardio_log"
    const val CALENDAR = "calendar"
}

@Composable
fun FitnessJournalApp() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(Routes.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(
                    Routes.DASHBOARD to Icons.Default.Home,
                    Routes.EXERCISE_LOG to Icons.Default.Favorite,
                    Routes.CALENDAR to Icons.Default.DateRange
                ).forEach { (route, icon) ->
                    NavigationBarItem(
                        selected = selectedItem == route,
                        onClick = {
                            selectedItem = route
                            navController.navigate(route) {
                                popUpTo(Routes.DASHBOARD)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = route) },
                        label = { Text(route.replace("_", " ").replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen { navController.navigate(Routes.CALENDAR) }
            }

            composable(Routes.EXERCISE_LOG) {
                ExerciseLogScreen(
                    onStrengthClick = { navController.navigate(Routes.STRENGTH_LOG) },
                    onCardioClick = { navController.navigate(Routes.CARDIO_LOG) }
                )
            }

            composable(Routes.STRENGTH_LOG) {
                val context = LocalContext.current
                val db = AppDatabase.getInstance(context) // ← Get the singleton database instance
                val dao = db.strengthExerciseDao()        // ← Get the DAO from the database

                StrengthLogScreen(
                    dao = dao,
                    onBack = { navController.navigateUp() }
                )
            }




            composable(Routes.CARDIO_LOG) { CardioLogScreen(onBack = { navController.navigateUp() }) }
            composable(Routes.CALENDAR) { CalendarScreen(onBack = { navController.navigateUp() }) }
        }
    }
}



// ------------------- DASHBOARD -------------------
@Composable
fun DashboardScreen(onCalendarClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to your Dashboard!", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCalendarClick) { Text("Open Calendar") }
        }
    }
}

// ------------------- EXERCISE LOG -------------------
@Composable
fun ExerciseLogScreen(onStrengthClick: () -> Unit, onCardioClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Exercise Log", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onStrengthClick, modifier = Modifier.fillMaxWidth()) { Text("Strength Log") }
        Button(onClick = onCardioClick, modifier = Modifier.fillMaxWidth()) { Text("Cardio Log") }
    }
}

// ------------------- CALENDAR -------------------
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusMonths(12)
    val endMonth = currentMonth.plusMonths(12)
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY
    )

    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Workout Calendar") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            HorizontalCalendar(
                state = calendarState,
                dayContent = { day ->
                    CalendarDayCell(
                        day = day,
                        isSelected = selectedDate == day.date,
                        isToday = day.date == today,
                        onClick = { selectedDate = day.date }
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            selectedDate?.let {
                Text(
                    text = "Selected: ${it.dayOfWeek}, ${it.dayOfMonth} ${it.month}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun CalendarDayCell(day: CalendarDay, isSelected: Boolean, isToday: Boolean, onClick: () -> Unit) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .border(1.dp, Color.LightGray, CircleShape)
            .background(bgColor, CircleShape)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

// ------------------- CARDIO LOG -------------------
@Composable
fun CardioLogScreen(
    exercises: List<String> = listOf("Running","Cycling","Rowing","Jump Rope","Swimming"),
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Cardio Log") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            }
        )
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises.size) { index ->
                val exercise = exercises[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* handle click if needed */ },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        exercise,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.fitnessjournalapplication.screens.*
import com.example.fitnessjournalapplication.ui.theme.FitnessJournalApplicationTheme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import com.example.fitnessjournalapplication.data.*
import com.example.fitnessjournalapplication.ui.screens.*

import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //The entire app UI is composed here. Keeping this minimal avoids logic clutter
        setContent {
            FitnessJournalApplicationTheme {
                FitnessJournalApp()
            }
        }
    }
}


/*Navigation route constants. Keeping routes centralised prevents typos and makes adding screens safer */
object Routes {
    const val DASHBOARD = "dashboard"
    const val EXERCISE_LOG = "exercise_log"
    const val CALENDAR = "calendar"

    // Strength & Cardio with date parameter
    const val STRENGTH_LOG_BASE = "strength_log"
    const val STRENGTH_LOG = "$STRENGTH_LOG_BASE/{date}"
    const val CARDIO_LOG_BASE = "cardio_log"
    const val CARDIO_LOG = "$CARDIO_LOG_BASE/{date}"

    const val NOTES_SCREEN = "notes_screen"
}

@Composable
fun FitnessJournalApp() {
    val navController = rememberNavController()
    //Tracks which bottom navigation is selected. This allows for consistent highlighting of the active tab
    var selectedItem by remember { mutableStateOf(Routes.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                //Centralised list keeps the bottom bar scalable
                listOf(
                    Routes.DASHBOARD to Icons.Default.Home,
                    Routes.EXERCISE_LOG to Icons.Default.FitnessCenter,
                    Routes.CALENDAR to Icons.Default.DateRange
                ).forEach { (route, icon) ->
                    NavigationBarItem(
                        selected = selectedItem == route,
                        onClick = {
                            //Preserves state and avoids duplication in back stack
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

            //Dashboard contains all charts separately to manage code easier
            composable(Routes.DASHBOARD) {
                DashboardScreen()
            }



            //Exercise log
            composable(Routes.EXERCISE_LOG) {
                ExerciseLogScreen(
                    onStrengthClick = {
                        /*Always default to today when opening logs because the user will most likely use todays logs*/
                        val today = LocalDate.now().toString()
                        navController.navigate("${Routes.STRENGTH_LOG_BASE}/$today")
                    },
                    onCardioClick = {
                        val today = LocalDate.now().toString()
                        navController.navigate("${Routes.CARDIO_LOG_BASE}/$today")
                    }
                )
            }

            //Strength log
            //Using a date argument allows navigating directly from the calendar.
            composable(
                route = Routes.STRENGTH_LOG,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val selectedDate = LocalDate.parse(dateString)
                val context = LocalContext.current
                val strengthDao = AppDatabase.getInstance(context).strengthExerciseDao()
                val strengthMasterDao = AppDatabase.getInstance(context).strengthMasterExerciseDao()


                StrengthLogScreen(
                    dao = strengthDao,
                    masterDao = strengthMasterDao,
                    selectedDate = selectedDate,
                    onBack = { navController.navigateUp() },
                    navController = navController
                )
            }

            //Cardio log
            //Using a date argument allows navigating directly from the calendar.
            composable(
                route = Routes.CARDIO_LOG,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                val selectedDate = LocalDate.parse(dateString)
                val context = LocalContext.current
                val cardioDao = AppDatabase.getInstance(context).cardioExerciseDao()
                val cardioMasterDao = AppDatabase.getInstance(context).cardioMasterExerciseDao()


                CardioLogScreen(
                    dao = cardioDao,
                    masterDao = cardioMasterDao,
                    selectedDate = selectedDate,
                    onBack = { navController.navigateUp() },
                    navController = navController
                )
            }

            //Calendar navigation
            /*This screen exists separately so
              the user can navigate freely through the calendar to check previous workouts*/
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onBack = { navController.navigateUp() },
                    onOpenStrengthForDate = { date -> navController.navigate("${Routes.STRENGTH_LOG_BASE}/${date}") },
                    onOpenCardioForDate = { date -> navController.navigate("${Routes.CARDIO_LOG_BASE}/${date}") }
                )
            }

            composable(
                route = "${Routes.NOTES_SCREEN}/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType }, // "strength" or "cardio"
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->

                val type = backStackEntry.arguments!!.getString("type")!!
                val id = backStackEntry.arguments!!.getInt("id")
                val context = LocalContext.current
                val db = AppDatabase.getInstance(context)

                NotesScreen(
                    type = type,
                    id = id,
                    strengthDao = db.strengthExerciseDao(),
                    cardioDao = db.cardioExerciseDao(),
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}



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


@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onOpenStrengthForDate: (LocalDate) -> Unit,
    onOpenCardioForDate: (LocalDate) -> Unit
) {
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
    val scope = rememberCoroutineScope() // Needed for animateScrollToMonth

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout Calendar") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            // Month navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    scope.launch {
                        val prevMonth = calendarState.firstVisibleMonth.yearMonth.minusMonths(1)
                        calendarState.animateScrollToMonth(prevMonth)
                    }
                }) {
                    Text("Prev Month")
                }

                val visibleMonth = calendarState.firstVisibleMonth.yearMonth
                Text(
                    text = "${visibleMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${visibleMonth.year}",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(onClick = {
                    scope.launch {
                        val nextMonth = calendarState.firstVisibleMonth.yearMonth.plusMonths(1)
                        calendarState.animateScrollToMonth(nextMonth)
                    }
                }) {
                    Text("Next Month")
                }
            }

            Spacer(Modifier.height(16.dp))

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

            selectedDate?.let { date ->
                Text(
                    text = "Selected: ${date.dayOfWeek}, ${date.dayOfMonth} ${date.month}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(onClick = { onOpenStrengthForDate(date) }) {
                        Text("Open Strength Log")
                    }
                    Button(onClick = { onOpenCardioForDate(date) }) {
                        Text("Open Cardio Log")
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
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

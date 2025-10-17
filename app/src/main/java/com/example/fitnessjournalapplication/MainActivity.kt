package com.example.fitnessjournalapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnessjournalapplication.ui.theme.FitnessJournalApplicationTheme

object Routes {
    const val DASHBOARD = "dashboard"
    const val EXERCISE_LOG = "exercise_log"
    const val STRENGTH_LOG = "strength_log"
    const val CARDIO_LOG = "cardio_log"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessJournalApplicationTheme {
                FitnessJournalApplicationApp()
            }
        }
    }
}

@Composable
fun FitnessJournalApplicationApp() {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    NavigationSuiteScaffold(
        navigationSuiteItems = {

            // Only show main bottom nav items
            listOf(
                Routes.DASHBOARD to Icons.Default.Home,
                Routes.EXERCISE_LOG to Icons.Default.Favorite
            ).forEach { (route, icon) ->
                // Check whether this route is selected

                val selected = (currentRoute == route)

                item(
                    icon = { Icon(icon, contentDescription = route) },
                    label = { Text(route.replace("_", " ").capitalize()) },
                    selected = selected,
                    onClick = {
                        // Navigate, avoiding multiple copies on back stack
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid stacking
                            popUpTo(Routes.DASHBOARD)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) {
        // The content area inside NavigationSuiteScaffold
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen()
                }
                composable(Routes.EXERCISE_LOG) {
                    ExerciseLogScreen(
                        onStrengthClick = { navController.navigate(Routes.STRENGTH_LOG) },
                        onCardioClick = { navController.navigate(Routes.CARDIO_LOG) }
                    )
                }
                composable(Routes.STRENGTH_LOG) {
                    StrengthTrainingLogScreen(onBack = { navController.navigateUp() })
                }
                composable(Routes.CARDIO_LOG) {
                    CardioLogScreen(onBack = { navController.navigateUp() })
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome to your Dashboard!")
    }
}

@Composable
fun ExerciseLogScreen(
    modifier: Modifier = Modifier,
    onStrengthClick: () -> Unit,
    onCardioClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Exercise Log",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onStrengthClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Strength Training Log")
        }

        Button(
            onClick = onCardioClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Cardio Log")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthTrainingLogScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Strength Training Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("This is the Strength Training Log screen.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardioLogScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Cardio Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("This is the Cardio Log screen.")
        }
    }
}

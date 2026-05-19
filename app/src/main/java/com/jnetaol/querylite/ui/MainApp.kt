package com.jnetaol.querylite.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jnetaol.querylite.ui.screens.AppViewModel
import com.jnetaol.querylite.ui.screens.browser.BrowserScreen
import com.jnetaol.querylite.ui.screens.diff.DiffScreen
import com.jnetaol.querylite.ui.screens.home.HomeScreen
import com.jnetaol.querylite.ui.screens.importexport.ImportExportScreen
import com.jnetaol.querylite.ui.screens.query.QueryScreen
import com.jnetaol.querylite.ui.screens.schema.SchemaScreen
import com.jnetaol.querylite.ui.screens.settings.SettingsScreen
import com.jnetaol.querylite.ui.theme.*

@Composable
fun MainApp(initialDbPath: String? = null) {
    val viewModel: AppViewModel = viewModel()
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem("Browser", Icons.Filled.TableChart, Icons.Outlined.TableChart, "browser"),
        BottomNavItem("Query", Icons.Filled.Code, Icons.Outlined.Code, "query"),
        BottomNavItem("Diff", Icons.Filled.CompareArrows, Icons.Outlined.CompareArrows, "diff"),
        BottomNavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
    )

    // Open external DB file if provided
    LaunchedEffect(initialDbPath) {
        if (initialDbPath != null) {
            viewModel.loadDatabase(initialDbPath)
        }
    }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = currentDestination?.route in listOf("home", "browser", "query", "diff", "settings")

            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = AccentPrimary,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            selected = selected,
                            onClick = {
                                if (currentDestination?.route != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentPrimary,
                                selectedTextColor = AccentPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextDisabled,
                                indicatorColor = AccentPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigatetoBrowser = { navController.navigate("browser") {
                        popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true
                    }},
                    onNavigateToQuery = { navController.navigate("query") {
                        popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true
                    }},
                    onNavigateToSchema = { navController.navigate("schema") {
                        popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true
                    }},
                    onNavigateToImportExport = { navController.navigate("importexport") {
                        popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true
                    }}
                )
            }
            composable("browser") {
                BrowserScreen(viewModel = viewModel, onNavigateBack = { navController.navigateUp() })
            }
            composable("query") {
                QueryScreen(viewModel = viewModel, onNavigateBack = { navController.navigateUp() })
            }
            composable("schema") {
                SchemaScreen(viewModel = viewModel, onNavigateBack = { navController.navigateUp() })
            }
            composable("diff") {
                DiffScreen(viewModel = viewModel, onNavigateBack = { navController.navigateUp() })
            }
            composable("importexport") {
                ImportExportScreen(viewModel = viewModel, onNavigateBack = { navController.navigateUp() })
            }
            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.navigateUp() })
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

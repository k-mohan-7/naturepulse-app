package com.simats.naturepulse.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.naturepulse.app
import com.simats.naturepulse.ui.auth.AuthViewModel
import com.simats.naturepulse.ui.auth.LoginScreen
import com.simats.naturepulse.ui.auth.RegisterScreen
import com.simats.naturepulse.ui.dashboard.DashboardScreen
import com.simats.naturepulse.ui.dashboard.DashboardViewModel
import com.simats.naturepulse.ui.notifications.NotificationsScreen
import com.simats.naturepulse.ui.notifications.NotificationsViewModel
import com.simats.naturepulse.ui.profile.ProfileScreen
import com.simats.naturepulse.ui.profile.ProfileViewModel
import com.simats.naturepulse.ui.reports.*
import com.simats.naturepulse.ui.theme.*
import kotlinx.coroutines.launch

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badge: Int = 0
)

@Composable
fun NaturePulseNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.app

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Shared ViewModels
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(app.authRepo, app.prefs))
    val dashboardVm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(app.reportRepo))
    val reportListVm: ReportListViewModel = viewModel(factory = ReportListViewModel.Factory(app.reportRepo))
    val myReportsVm: ReportListViewModel = viewModel(
        key = "my_reports",
        factory = ReportListViewModel.Factory(app.reportRepo, myReports = true)
    )
    val reportDetailVm: ReportDetailViewModel = viewModel(factory = ReportDetailViewModel.Factory(app.reportRepo))
    val addReportVm: AddReportViewModel = viewModel(factory = AddReportViewModel.Factory(app.reportRepo))
    val notifVm: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory(app.notifRepo))
    val profileVm: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(app.userRepo, app.authRepo, app.prefs))

    // User state for role checks
    val userState by profileVm.state.collectAsState()
    val currentUserId = userState.user?.id ?: 0

    // Determine start destination
    val startRoute = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val token = app.prefs.getTokenNow()
        startRoute.value = if (token != null) NavRoute.Dashboard.route else NavRoute.Login.route
    }
    val start = startRoute.value ?: return

    // Bottom nav items
    val notifState by notifVm.state.collectAsState()
    val bottomItems = listOf(
        BottomNavItem(NavRoute.Dashboard.route, "Home", Icons.Default.Home),
        BottomNavItem(NavRoute.Reports.route, "Reports", Icons.Default.Nature),
        BottomNavItem(NavRoute.MyReports.route, "My Reports", Icons.Default.Folder),
        BottomNavItem(NavRoute.Notifications.route, "Alerts", Icons.Default.Notifications, notifState.unread),
        BottomNavItem(NavRoute.Profile.route, "Profile", Icons.Default.Person)
    )

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val mainRoutes = setOf(
        NavRoute.Dashboard.route, NavRoute.Reports.route,
        NavRoute.MyReports.route, NavRoute.Notifications.route, NavRoute.Profile.route
    )
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = SurfaceDark) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.badge > 0) Badge { Text(item.badge.toString()) }
                                    }
                                ) {
                                    Icon(item.icon, item.label)
                                }
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ForestGreen,
                                selectedTextColor = ForestGreen,
                                unselectedIconColor = OnSurfaceFaint,
                                unselectedTextColor = OnSurfaceFaint,
                                indicatorColor = LightGold
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = start,
            modifier = Modifier.padding(padding)
        ) {
            // ── Auth ─────────────────────────────────────────────────────────
            composable(NavRoute.Login.route) {
                LoginScreen(
                    viewModel = authVm,
                    onLoginSuccess = {
                        profileVm.load()
                        notifVm.load()
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(NavRoute.Register.route) }
                )
            }

            composable(NavRoute.Register.route) {
                RegisterScreen(
                    viewModel = authVm,
                    onRegisterSuccess = {
                        profileVm.load()
                        notifVm.load()
                        navController.navigate(NavRoute.Dashboard.route) {
                            popUpTo(NavRoute.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // ── Dashboard ─────────────────────────────────────────────────────
            composable(NavRoute.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardVm,
                    userName = userState.user?.name ?: "Citizen",
                    onReportClick = { navController.navigate(NavRoute.ReportDetail.withId(it)) },
                    onAddReport = { navController.navigate(NavRoute.AddReport.route) },
                    onLocationReady = { lat, lng -> dashboardVm.load(lat, lng) },
                    snackbarHostState = snackbarHostState
                )
            }

            // ── Reports ───────────────────────────────────────────────────────
            composable(NavRoute.Reports.route) {
                ReportListScreen(
                    viewModel = reportListVm,
                    title = "All Reports",
                    onReportClick = { navController.navigate(NavRoute.ReportDetail.withId(it)) },
                    onAddReport = { navController.navigate(NavRoute.AddReport.route) }
                )
            }

            // ── My Reports ────────────────────────────────────────────────────
            composable(NavRoute.MyReports.route) {
                ReportListScreen(
                    viewModel = myReportsVm,
                    title = "My Reports",
                    onReportClick = { navController.navigate(NavRoute.ReportDetail.withId(it)) },
                    onAddReport = { navController.navigate(NavRoute.AddReport.route) }
                )
            }

            // ── Add Report ────────────────────────────────────────────────────
            composable(NavRoute.AddReport.route) {
                AddReportScreen(
                    viewModel = addReportVm,
                    onSuccess = { reportId ->
                        navController.navigate(NavRoute.ReportDetail.withId(reportId)) {
                            popUpTo(NavRoute.Dashboard.route)
                        }
                    },
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState
                )
            }

            // ── Report Detail ─────────────────────────────────────────────────
            composable(
                route = NavRoute.ReportDetail.route,
                arguments = listOf(navArgument("reportId") { type = NavType.IntType })
            ) { backStack ->
                val reportId = backStack.arguments?.getInt("reportId") ?: return@composable
                ReportDetailScreen(
                    viewModel = reportDetailVm,
                    reportId = reportId,
                    currentUserId = currentUserId,
                    onBack = { navController.popBackStack() },
                    snackbarHostState = snackbarHostState
                )
            }

            // ── Notifications ─────────────────────────────────────────────────
            composable(NavRoute.Notifications.route) {
                NotificationsScreen(
                    viewModel = notifVm,
                    onOpenReport = { navController.navigate(NavRoute.ReportDetail.withId(it)) }
                )
            }

            // ── Profile ───────────────────────────────────────────────────────
            composable(NavRoute.Profile.route) {
                ProfileScreen(
                    viewModel = profileVm,
                    onLogout = {
                        navController.navigate(NavRoute.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

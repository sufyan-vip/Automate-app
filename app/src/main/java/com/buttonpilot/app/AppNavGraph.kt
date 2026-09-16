package com.buttonpilot.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.buttonpilot.app.feature.dashboard.DashboardScreen
import com.buttonpilot.app.feature.diagnostics.BackgroundReliabilityScreen
import com.buttonpilot.app.feature.diagnostics.DiagnosticsScreen
import com.buttonpilot.app.feature.onboarding.OnboardingScreen
import com.buttonpilot.app.feature.recorder.RecordingsListScreen
import com.buttonpilot.app.feature.recorder.RecorderScreen
import com.buttonpilot.app.feature.settings.PermissionCenterScreen
import com.buttonpilot.app.feature.settings.SettingsScreen
import com.buttonpilot.app.feature.shortcuts.ShortcutBuilderScreen
import com.buttonpilot.app.feature.shortcuts.TestShortcutScreen
import com.buttonpilot.app.feature.dashboard.DashboardViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val onboardingCompleted by dashboardViewModel.preferencesManager.onboardingCompleted.collectAsState(initial = true)

    val startDestination = if (!onboardingCompleted) "onboarding" else "dashboard"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onFinished = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToRecorder = { navController.navigate("recorder") },
                onNavigateToRecordings = { navController.navigate("recordings") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToDiagnostics = { navController.navigate("diagnostics") },
                onNavigateToPermissionCenter = { navController.navigate("permission_center") },
                onNavigateToTestShortcut = { navController.navigate("test_shortcut") },
                onNavigateToShortcutBuilder = { navController.navigate("shortcut_builder") },
                onNavigateToBackgroundReliability = { navController.navigate("background_reliability") },
                onNavigateToOnboarding = { navController.navigate("onboarding") }
            )
        }
        composable("recorder") {
            RecorderScreen(onBack = { navController.popBackStack() })
        }
        composable("recordings") {
            RecordingsListScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("diagnostics") {
            DiagnosticsScreen(onBack = { navController.popBackStack() })
        }
        composable("permission_center") {
            PermissionCenterScreen(onBack = { navController.popBackStack() })
        }
        composable("test_shortcut") {
            TestShortcutScreen(onBack = { navController.popBackStack() })
        }
        composable("shortcut_builder") {
            ShortcutBuilderScreen(onBack = { navController.popBackStack() })
        }
        composable("background_reliability") {
            BackgroundReliabilityScreen(onBack = { navController.popBackStack() })
        }
    }
}

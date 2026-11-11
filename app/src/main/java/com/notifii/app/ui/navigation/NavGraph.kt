package com.notifii.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.notifii.app.ui.screens.history.HistoryScreen
import com.notifii.app.ui.screens.home.HomeScreen
import com.notifii.app.ui.screens.onboarding.OnboardingScreen
import com.notifii.app.ui.screens.rules.RuleEditorScreen
import com.notifii.app.ui.screens.rules.RuleListScreen
import com.notifii.app.ui.screens.settings.SettingsScreen

@Composable
fun NotifiiNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToRules = { navController.navigate(Screen.RuleList.route) },
                onNavigateToRuleEditor = { ruleId ->
                    navController.navigate(Screen.RuleEditor.createRoute(ruleId))
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.RuleList.route) {
            RuleListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { ruleId ->
                    navController.navigate(Screen.RuleEditor.createRoute(ruleId))
                }
            )
        }

        composable(
            route = Screen.RuleEditor.route,
            arguments = listOf(
                navArgument("ruleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            RuleEditorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

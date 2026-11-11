package com.notifii.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NotifiiRoot(startTab: StateFlow<String>) {
    val context = LocalContext.current
    val vm: NotifiiViewModel = viewModel()
    val sheets = remember { Sheets() }
    val nav = rememberNavController()
    val start = remember { if (listenerEnabled(context)) "main" else "onboarding" }
    val tab by startTab.collectAsState()
    val d = 280

    NavHost(
        navController = nav,
        startDestination = start,
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(d), initialOffset = { it / 4 }) + fadeIn(tween(d)) },
        exitTransition = { slideOutOfContainer(SlideDirection.Start, tween(d), targetOffset = { it / 4 }) + fadeOut(tween(d / 2)) },
        popEnterTransition = { slideIntoContainer(SlideDirection.End, tween(d), initialOffset = { it / 4 }) + fadeIn(tween(d)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(d), targetOffset = { it / 4 }) + fadeOut(tween(d / 2)) },
    ) {
        composable("onboarding") { OnboardingScreen { nav.navigate("main") { popUpTo("onboarding") { inclusive = true } } } }
        composable("main") { MainScreen(vm, sheets, tab, onSettings = { nav.navigate("settings") }) }
        composable("settings") { SettingsScreen(vm, sheets, onBack = { nav.popBackStack() }) }
    }
    SheetHost(vm, sheets)
}

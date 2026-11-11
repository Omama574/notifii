package com.notifii.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object RuleList : Screen("rules")
    data object RuleEditor : Screen("rule_editor?ruleId={ruleId}") {
        fun createRoute(ruleId: Long? = null): String {
            return if (ruleId != null && ruleId > 0) "rule_editor?ruleId=$ruleId" else "rule_editor"
        }
    }
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Onboarding : Screen("onboarding")
}

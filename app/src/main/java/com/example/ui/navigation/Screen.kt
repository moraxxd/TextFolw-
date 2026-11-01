package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Onboarding : Screen("onboarding")
    object OverlayPermission : Screen("overlay_permission")
    object MicPermission : Screen("mic_permission")
    object TranslationModel : Screen("translation_model")
    object History : Screen("history")
    object Settings : Screen("settings")
}

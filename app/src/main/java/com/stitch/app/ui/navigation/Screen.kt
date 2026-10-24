package com.stitch.app.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Friends : Screen("friends")
    object AddFriend : Screen("add_friend")
    object Pacts : Screen("pacts")
    object CreatePact : Screen("create_pact")
    object Schedule : Screen("schedule")
    object Settings : Screen("settings")
}

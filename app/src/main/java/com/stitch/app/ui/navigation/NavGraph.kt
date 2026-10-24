package com.stitch.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.UsageRepository
import com.stitch.app.ui.screens.auth.LoginScreen
import com.stitch.app.ui.screens.auth.SignUpScreen
import com.stitch.app.ui.screens.friends.AddFriendScreen
import com.stitch.app.ui.screens.friends.FriendsScreen
import com.stitch.app.ui.screens.home.HomeScreen
import com.stitch.app.ui.screens.pacts.CreatePactScreen
import com.stitch.app.ui.screens.pacts.PactsScreen
import com.stitch.app.ui.screens.schedule.ScheduleScreen
import com.stitch.app.ui.screens.settings.SettingsScreen

@Composable
fun StitchNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    startDestination: String,
    authRepository: AuthRepository,
    friendsRepository: FriendsRepository,
    pactsRepository: PactsRepository,
    usageRepository: UsageRepository
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Friends.route) {
            FriendsScreen(navController = navController)
        }
        composable(Screen.AddFriend.route) {
            AddFriendScreen(
                navController = navController,
                authRepository = authRepository,
                friendsRepository = friendsRepository
            )
        }
        composable(Screen.Pacts.route) {
            PactsScreen(navController = navController)
        }
        composable(Screen.CreatePact.route) {
            CreatePactScreen(
                navController = navController,
                pactsRepository = pactsRepository,
                friendsRepository = friendsRepository,
                usageRepository = usageRepository
            )
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                authRepository = authRepository,
                usageRepository = usageRepository
            )
        }
    }
}

package com.example.projectotherversion.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectotherversion.presentation.screens.chat.ChatScreen
import com.example.projectotherversion.presentation.screens.chat.ChatViewModel
import com.example.projectotherversion.presentation.screens.complaints.ComplaintsScreen
import com.example.projectotherversion.presentation.screens.complaints.ComplaintsViewModel
import com.example.projectotherversion.presentation.screens.dashboard.DashboardScreen
import com.example.projectotherversion.presentation.screens.dashboard.DashboardViewModel
import com.example.projectotherversion.presentation.screens.login.LoginScreen
import com.example.projectotherversion.presentation.screens.login.LoginViewModel
import com.example.projectotherversion.presentation.screens.myposts.MyPostsScreen
import com.example.projectotherversion.presentation.screens.myposts.MyPostsViewModel
import com.example.projectotherversion.presentation.screens.register.RegisterScreen
import com.example.projectotherversion.presentation.screens.register.RegisterViewModel
import com.example.projectotherversion.presentation.screens.settings.SettingsScreen
import com.example.projectotherversion.presentation.screens.settings.SettingsViewModel
import com.example.projectotherversion.presentation.screens.visitor.VisitorScreen
import com.example.projectotherversion.presentation.screens.workrequests.WorkRequestsScreen
import com.example.projectotherversion.presentation.screens.workrequests.WorkRequestsViewModel
import com.example.projectotherversion.presentation.screens.rating.RatingScreen
import com.example.projectotherversion.presentation.screens.rating.RatingViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Visitor : Screen("visitor")
    object Dashboard : Screen("dashboard")
    object Chat : Screen("chat/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String) = "chat/$userId/$userName"
    }
    object Settings : Screen("settings")
    object Complaints : Screen("complaints")
    object WorkRequests : Screen("work_requests")
    object MyPosts : Screen("my_posts")
    object Rating : Screen("rating/{artisanId}/{artisanName}") {
        fun createRoute(artisanId: String, artisanName: String) = "rating/$artisanId/$artisanName"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // شاشة تسجيل الدخول
        composable(Screen.Login.route) {
            val viewModel = hiltViewModel<LoginViewModel>()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToVisitor = { navController.navigate(Screen.Visitor.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // شاشة التسجيل
        composable(Screen.Register.route) {
            val viewModel = hiltViewModel<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // شاشة الزائر
        composable(Screen.Visitor.route) {
            VisitorScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginRequest = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Visitor.route) { inclusive = true }
                    }
                }
            )
        }

        // شاشة لوحة التحكم الرئيسية
        composable(Screen.Dashboard.route) {
            val viewModel = hiltViewModel<DashboardViewModel>()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToChat = { userId, userName ->
                    navController.navigate(Screen.Chat.createRoute(userId, userName))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToComplaints = { navController.navigate(Screen.Complaints.route) },
                onNavigateToWorkRequests = { navController.navigate(Screen.WorkRequests.route) },
                onNavigateToMyPosts = { navController.navigate(Screen.MyPosts.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // شاشة المحادثة
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val userName = backStackEntry.arguments?.getString("userName") ?: return@composable

            val viewModel = hiltViewModel<ChatViewModel>()
            ChatScreen(
                viewModel = viewModel,
                otherUserId = userId,
                otherUserName = userName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRating = { id, name ->
                    navController.navigate(Screen.Rating.createRoute(id, name))
                }
            )
        }

        // شاشة الإعدادات
        composable(Screen.Settings.route) {
            val viewModel = hiltViewModel<SettingsViewModel>()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // شاشة الشكاوى
        composable(Screen.Complaints.route) {
            val viewModel = hiltViewModel<ComplaintsViewModel>()
            ComplaintsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // شاشة طلبات العمل (للحرفي)
        composable(Screen.WorkRequests.route) {
            val viewModel = hiltViewModel<WorkRequestsViewModel>()
            WorkRequestsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onChatClick = { userId, userName ->
                    navController.navigate(Screen.Chat.createRoute(userId, userName))
                }
            )
        }

        // شاشة منشوراتي (للزبون)
        composable(Screen.MyPosts.route) {
            val viewModel = hiltViewModel<MyPostsViewModel>()
            MyPostsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // شاشة التقييم
        composable(
            route = Screen.Rating.route,
            arguments = listOf(
                navArgument("artisanId") { type = NavType.StringType },
                navArgument("artisanName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val artisanId = backStackEntry.arguments?.getString("artisanId") ?: return@composable
            val artisanName = backStackEntry.arguments?.getString("artisanName") ?: return@composable
            
            val viewModel = hiltViewModel<RatingViewModel>()
            RatingScreen(
                viewModel = viewModel,
                artisanId = artisanId,
                artisanName = artisanName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

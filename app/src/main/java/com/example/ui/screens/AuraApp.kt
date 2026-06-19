package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.viewmodel.AuraViewModel

@Composable
fun AuraApp(viewModel: AuraViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeDestination) {
        composable<HomeDestination> {
            HomeScreen(
                onNavigateToEntry = { entryId ->
                    navController.navigate(EntryDestination(entryId ?: -1))
                },
                viewModel = viewModel
            )
        }
        composable<EntryDestination> { backStackEntry ->
            val route = backStackEntry.toRoute<EntryDestination>()
            val entryId = if (route.entryId == -1) null else route.entryId
            EntryScreen(
                entryId = entryId,
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

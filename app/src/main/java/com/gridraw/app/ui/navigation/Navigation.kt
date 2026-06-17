package com.gridraw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gridraw.app.ui.screens.EditorScreen
import com.gridraw.app.ui.screens.HomeScreen
import com.gridraw.app.ui.screens.ProjectsScreen
import com.gridraw.app.viewmodel.EditorViewModel
import com.gridraw.app.viewmodel.ProjectViewModel

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Editor   : Screen("editor")
    object Projects : Screen("projects")
}

@Composable
fun GridRawNavGraph(navController: NavHostController) {
    val editorViewModel: EditorViewModel = viewModel()
    val projectViewModel: ProjectViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNewProject = { navController.navigate(Screen.Editor.route) },
                onOpenProjects = { navController.navigate(Screen.Projects.route) },
                editorViewModel = editorViewModel
            )
        }

        composable(Screen.Editor.route) {
            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Projects.route) {
            ProjectsScreen(
                projectViewModel = projectViewModel,
                onOpenProject = { navController.navigate(Screen.Editor.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

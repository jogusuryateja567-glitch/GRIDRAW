package com.gridraw.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gridraw.app.ui.screens.CropSetupScreen
import com.gridraw.app.ui.screens.EditorScreen
import com.gridraw.app.ui.screens.HomeScreen
import com.gridraw.app.ui.screens.ProjectsScreen
import com.gridraw.app.viewmodel.EditorViewModel
import com.gridraw.app.viewmodel.ProjectViewModel

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object CropSetup: Screen("crop")
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
                editorViewModel = editorViewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.CropSetup.route) {
            val state by editorViewModel.state.collectAsState()
            val bitmap = state.pendingCropBitmap
            if (bitmap != null) {
                CropSetupScreen(
                    bitmap = bitmap,
                    initialPaperSize = state.paperSize,
                    initialOrientation = state.orientation,
                    onConfirm = { b, ps, o ->
                        editorViewModel.applyCrop(b, ps, o)
                        navController.navigate(Screen.Editor.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            } else {
                // Should not happen, but fallback
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
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

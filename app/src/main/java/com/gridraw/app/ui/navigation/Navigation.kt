package com.gridraw.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gridraw.app.ui.screens.CropSetupScreen
import com.gridraw.app.ui.screens.EditorScreen
import com.gridraw.app.ui.screens.HomeScreen
import com.gridraw.app.ui.screens.ProjectsScreen
import com.gridraw.app.ui.theme.AccentBlue
import com.gridraw.app.ui.theme.BgRoot
import com.gridraw.app.ui.theme.TextMuted
import com.gridraw.app.viewmodel.EditorViewModel
import com.gridraw.app.viewmodel.ProjectViewModel
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object CropSetup : Screen("crop")
    object Editor    : Screen("editor")
    object Projects  : Screen("projects")
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
                    onConfirm = { croppedBitmap, ps, ori ->
                        editorViewModel.applyCrop(
                            context = navController.context,
                            bitmap = croppedBitmap,
                            paperSize = ps,
                            orientation = ori,
                            sourceUri = state.pendingImageUri
                        )
                        navController.navigate(Screen.Editor.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            } else if (state.isLoading) {
                // Show loading state while bitmap is being decoded (prevents premature popBack)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgRoot),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                }
            } else {
                // Bitmap is truly null and not loading — go back after a brief delay
                // to prevent instant flash
                LaunchedEffect(Unit) {
                    delay(100)
                    navController.popBackStack()
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgRoot),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading…", color = TextMuted, fontSize = 14.sp)
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
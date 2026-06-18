package com.gridraw.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.gridraw.app.ui.navigation.GridRawNavGraph
import com.gridraw.app.ui.theme.GridRawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request highest available refresh rate (120Hz on supported devices)
        window.attributes = window.attributes.also {
            it.preferredRefreshRate = 120f
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            GridRawTheme {
                GridRawApp()
            }
        }
    }
}

@Composable
fun GridRawApp() {
    val navController = rememberNavController()
    // Pure black background — prevents white flash on navigation
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        GridRawNavGraph(navController = navController)
    }
}


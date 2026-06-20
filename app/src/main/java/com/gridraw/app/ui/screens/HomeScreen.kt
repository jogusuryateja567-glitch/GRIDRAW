package com.gridraw.app.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.navigation.Screen
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.EditorViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNewProject: () -> Unit,
    onOpenProjects: () -> Unit,
    editorViewModel: EditorViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val hazeState = remember { HazeState() }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editorViewModel.loadImageFromUri(context, it) { success ->
                if (success) onNavigate(Screen.CropSetup.route)
            }
        }
    }

    // FIX: camera flow — toggle camera mode then navigate to editor directly
    // onNewProject() goes to EditorScreen; the editor then checks isCameraMode
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            editorViewModel.toggleCameraMode()
            onNewProject()  // navigate to EditorScreen where camera preview renders
        }
        // Silently no-op on deny — could show a Snackbar via a shared state if desired
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Haze source background layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            // Large dark accent circle — top left
            Box(
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = (-150).dp, y = (-200).dp)
                    .background(Color(0xFF1A1A1C), CircleShape)
            )
            // Smaller accent circle — bottom right
            Box(
                modifier = Modifier
                    .size(350.dp)
                    .offset(x = 100.dp, y = 600.dp)
                    .background(Color(0xFF141415), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .border(1.dp, BorderGlass, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridOn,
                    contentDescription = null,
                    tint = TextMain,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "GRIDRAW",
                color = TextMain,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Spatial Artist Tool",
                color = TextMuted,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(72.dp))

            // ── Primary CTA — Import Reference ────────────────────────────────
            SpatialCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                hazeState = hazeState,
                onClick = { imageLauncher.launch("image/*") }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = TextMain,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Import Reference",
                        color = TextMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Browse photo library",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Secondary Action Pair ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SpatialActionCard(
                    modifier = Modifier.weight(1f),
                    hazeState = hazeState,
                    icon = Icons.Rounded.CenterFocusWeak,
                    label = "AR Drawing",
                    sublabel = "Live camera",
                    onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) }
                )
                SpatialActionCard(
                    modifier = Modifier.weight(1f),
                    hazeState = hazeState,
                    icon = Icons.Rounded.FolderOpen,
                    label = "Projects",
                    sublabel = "Saved work",
                    onClick = onOpenProjects
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── Feature Pills ─────────────────────────────────────────────────
            Text(
                "FEATURES",
                color = TextDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(14.dp))
            FeaturePillRow(hazeState)
        }
    }
}

// ── SpatialCard ───────────────────────────────────────────────────────────────

@Composable
fun SpatialCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(22.dp),
                style = HazeStyle(blurRadius = 30.dp, tint = BgCard)
            )
            .border(1.dp, BorderGlass, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ── SpatialActionCard ─────────────────────────────────────────────────────────

@Composable
private fun SpatialActionCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    icon: ImageVector,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit,
) {
    SpatialCard(
        modifier = modifier.height(100.dp),
        hazeState = hazeState,
        onClick = onClick
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = TextMain, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (sublabel != null) {
                Spacer(Modifier.height(2.dp))
                Text(sublabel, color = TextDim, fontSize = 11.sp)
            }
        }
    }
}

// ── FeaturePillRow ────────────────────────────────────────────────────────────

@Composable
private fun FeaturePillRow(hazeState: HazeState) {
    val features = listOf(
        "Grid Overlay",
        "Palette Extract",
        "Live Camera",
        "Ruler Tool",
        "Undo History",
        "Custom Paper"
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { feature ->
            Box(
                modifier = Modifier
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(100.dp),
                        style = HazeStyle(blurRadius = 15.dp, tint = BgCard)
                    )
                    .border(1.dp, BorderLight, RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    feature,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
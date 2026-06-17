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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.EditorViewModel
import com.gridraw.app.ui.navigation.Screen

// ──────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNewProject: () -> Unit,
    onOpenProjects: () -> Unit,
    editorViewModel: EditorViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editorViewModel.loadImageFromUri(context, it) { success ->
                if (success) onNavigate(Screen.CropSetup.route)
            }
        }
    }

    // Camera permission
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            editorViewModel.toggleCameraMode()
            onNewProject()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Subtle monochromatic background shapes
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant large dark gray circle in top left
            Box(
                modifier = Modifier
                    .size(500.dp)
                    .offset(x = (-150).dp, y = (-200).dp)
                    .background(Color(0xFF1A1A1C), CircleShape)
            )
            // Smaller dark circle bottom right
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
            Spacer(Modifier.height(60.dp))

            // ── Logo (Spatial Minimalist) ────────────────────────────────────
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Glass ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, BorderGlass, CircleShape)
                )

                // Inner icon
                Icon(
                    imageVector = Icons.Rounded.GridOn,
                    contentDescription = null,
                    tint = TextMain,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Wordmark ──────────────────────────────────────────────────────
            Text(
                "GRIDRAW",
                color = TextMain,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Spatial Artist Tool",
                color = TextMuted,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(80.dp))

            // ── Primary CTA ───────────────────────────────────────────────────
            SpatialCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                onClick = { imageLauncher.launch("image/*") }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = TextMain,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Import Reference",
                        color = TextMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Browse photo library",
                        color = TextDim,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Secondary Actions ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera
                SpatialActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.PhotoCamera,
                    label = "Camera",
                    onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) }
                )

                // Projects
                SpatialActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.FolderOpen,
                    label = "Projects",
                    onClick = onOpenProjects
                )
            }

            Spacer(Modifier.height(50.dp))

            // ── Feature Pills ─────────────────────────────────────────────────
            Text(
                "FEATURES",
                color = TextDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(16.dp))
            FeaturePillRow()
        }
    }
}

@Composable
fun SpatialCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Spring animation for press scale
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(BgCard, RoundedCornerShape(24.dp))
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun SpatialActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    SpatialCard(
        modifier = modifier.height(100.dp),
        onClick = onClick
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = TextMain, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FeaturePillRow() {
    val features = listOf(
        "Grid Overlay",
        "Palette Extract",
        "Live Camera",
        "Ruler Tool",
        "Undo History"
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        features.forEach { feature ->
            Box(
                modifier = Modifier
                    .background(BgCard, RoundedCornerShape(100.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(feature, color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

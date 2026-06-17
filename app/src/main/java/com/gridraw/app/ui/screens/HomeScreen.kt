package com.gridraw.app.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.EditorViewModel

// ──────────────────────────────────────────────────────────────────────────────
// HomeScreen
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNewProject: () -> Unit,
    onOpenProjects: () -> Unit,
    editorViewModel: EditorViewModel,
) {
    val context = LocalContext.current

    // Image picker
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editorViewModel.loadImageFromUri(context, it)
            onNewProject()
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

    // Animated rotation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // Background glow orbs
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset((-80).dp, (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(80.dp, 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentPurple.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Logo ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                        .border(
                            1.dp,
                            Brush.sweepGradient(
                                listOf(
                                    AccentBlue.copy(alpha = 0f),
                                    AccentBlue,
                                    AccentPurple,
                                    AccentBlue.copy(alpha = 0f)
                                )
                            ),
                            CircleShape
                        )
                )

                // Inner icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AccentBlue.copy(alpha = 0.2f), AccentPurple.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridOn,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Wordmark ──────────────────────────────────────────────────────
            Text(
                "GRIDRAW",
                color = TextMain,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(
                "Professional Artist Grid Tool",
                color = TextMuted,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(60.dp))

            // ── Primary CTA ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AccentBlue.copy(alpha = 0.12f),
                                AccentPurple.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(AccentBlue.copy(alpha = 0.4f), AccentPurple.copy(alpha = 0.3f))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { imageLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Import Reference Image",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Tap to browse gallery",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Secondary Actions ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.PhotoCamera,
                    label = "Camera",
                    sublabel = "Live Reference",
                    iconTint = AccentCyan,
                    onClick = {
                        cameraPermLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                // Projects
                HomeActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.FolderOpen,
                    label = "Projects",
                    sublabel = "Saved Work",
                    iconTint = AccentPurple,
                    onClick = onOpenProjects
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Feature Pills ─────────────────────────────────────────────────
            Text(
                "FEATURES",
                color = TextDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(16.dp))
            FeaturePillRow()
        }
    }
}

@Composable
private fun HomeActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    sublabel: String,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(sublabel, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun FeaturePillRow() {
    val features = listOf(
        "📐 Grid Overlay",
        "🎨 Palette Extract",
        "📸 Live Camera",
        "📏 Ruler Tool",
        "↩️ Undo History",
        "💾 Save Projects"
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { feature ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(BgInput)
                    .border(1.dp, BorderLight, RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(feature, color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

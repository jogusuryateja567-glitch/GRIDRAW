package com.gridraw.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.gridraw.app.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

// ─────────────────────────────────────────────────────────────────────────────
// Floating Tool Dock
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ToolDock(
    hazeState: HazeState,
    visible: Boolean,
    showRuler: Boolean,
    showGrid: Boolean,
    isCameraMode: Boolean = false,
    onFitScreen: () -> Unit,
    onToggleGrid: () -> Unit,
    onOpenPanel: () -> Unit,
    onToggleRuler: () -> Unit,
    onCameraMode: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    // FIX: was animating from `it * 2` (double height = huge jarring jump)
    // Now uses a smooth spring slide from just below screen
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(180, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(100.dp),
                        style = HazeStyle(blurRadius = 30.dp, tint = BgCard)
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(100.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DockButton(
                    icon = Icons.Rounded.FitScreen,
                    label = "Fit",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFitScreen()
                    }
                )

                DockDivider()

                DockButton(
                    icon = Icons.Rounded.GridOn,
                    label = "Grid",
                    isActive = showGrid,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleGrid()
                    }
                )

                DockButton(
                    icon = Icons.Rounded.Straighten,
                    label = "Ruler",
                    isActive = showRuler,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleRuler()
                    }
                )

                DockButton(
                    icon = Icons.Rounded.PhotoCamera,
                    label = "AR",
                    isActive = isCameraMode,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCameraMode()
                    }
                )

                DockDivider()

                // Settings button — slightly more prominent
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenPanel()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Settings",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DockButton(
    icon: ImageVector,
    label: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 1f, // reserved for press animation
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dock_btn_scale"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color.White.copy(alpha = 0.18f)
                else Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                !enabled -> TextDim
                isActive -> Color.White
                else -> TextMain
            },
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun DockDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(BorderLight)
    )
}
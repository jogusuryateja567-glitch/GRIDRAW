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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gridraw.app.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

// ─────────────────────────────────────────────────────────────────────────────
// ToolDock — Premium Floating Tool Bar with Glassmorphic Design
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ToolDock(
    hazeState: HazeState,
    visible: Boolean,
    showRuler: Boolean,
    showGrid: Boolean,
    isCameraMode: Boolean = false,
    isArProject: Boolean = false,
    onFitScreen: () -> Unit,
    onToggleGrid: () -> Unit,
    onOpenPanel: () -> Unit,
    onToggleRuler: () -> Unit,
    onCameraMode: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it + 24 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(280)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it + 24 },
            animationSpec = tween(200, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glassmorphic dock container
            Row(
                modifier = Modifier
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(28.dp),
                        style = HazeStyle(
                            blurRadius = 30.dp,
                            tint = Color(0x1A191924),
                            blurredCornerRadius = 28.dp
                        )
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                    .background(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .semantics {
                        contentDescription = "Tool dock with grid, ruler, and settings"
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Fit to screen button
                DockButton(
                    icon = Icons.Rounded.FitScreen,
                    label = "Fit to Screen",
                    contentDescription = "Fit entire canvas to screen",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFitScreen()
                    }
                )

                DockDivider()

                // Grid toggle
                DockButton(
                    icon = Icons.Rounded.GridOn,
                    label = "Grid",
                    contentDescription = if (showGrid) "Hide grid overlay" else "Show grid overlay",
                    isActive = showGrid,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleGrid()
                    }
                )

                // Ruler toggle
                DockButton(
                    icon = Icons.Rounded.Straighten,
                    label = "Ruler",
                    contentDescription = if (showRuler) "Hide measurement tool" else "Show measurement tool",
                    isActive = showRuler,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleRuler()
                    }
                )

                // Camera mode button (only shown when active or it's an AR project)
                if (isCameraMode || isArProject) {
                    DockButton(
                        icon = Icons.Rounded.PhotoCamera,
                        label = "AR Mode",
                        contentDescription = "Toggle AR camera mode",
                        isActive = isCameraMode,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCameraMode()
                        }
                    )
                }

                DockDivider()

                // Settings button — premium style with enhanced visual weight
                SettingsButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenPanel()
                    }
                )
            }
        }
    }
}

/**
 * Individual tool dock button with ripple, active states, and haptic feedback
 */
@Composable
fun DockButton(
    icon: ImageVector,
    label: String = "",
    contentDescription: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dock_button_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            Color.White.copy(alpha = 0.25f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "dock_button_bg"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            !enabled -> TextDim
            isActive -> Color.White
            else -> TextMain
        },
        animationSpec = tween(durationMillis = 200),
        label = "dock_button_icon"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .semantics {
                this.contentDescription = contentDescription ?: label
            },
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Premium settings button with enhanced visual hierarchy
 */
@Composable
fun SettingsButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "settings_button_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                color = Color.White.copy(alpha = 0.95f),
                shape = CircleShape
            )
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shadowElevation = 8f
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            }
            .semantics {
                contentDescription = "Open settings panel"
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Visual divider between dock button groups
 */
@Composable
fun DockDivider() {
    Box(
        modifier = Modifier
            .width(1.5f)
            .height(24.dp)
            .background(Color.White.copy(alpha = 0.15f))
    )
}
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

// ──────────────────────────────────────────────────────────────────────────────
// Floating Tool Dock
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ToolDock(
    hazeState: HazeState,
    visible: Boolean,
    zoomPercent: Int,
    canUndo: Boolean,
    showRuler: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFitScreen: () -> Unit,
    onUndo: () -> Unit,
    onOpenPanel: () -> Unit,
    onToggleRuler: () -> Unit,
    onCameraMode: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY  = { it * 2 }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glass pill
            Row(
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .hazeChild(state = hazeState, shape = RoundedCornerShape(100.dp), style = HazeStyle(blurRadius = 30.dp, tint = BgPanel.copy(alpha = 0.5f)))
                    .border(1.dp, BorderGlass, RoundedCornerShape(100.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DockButton(
                    icon = Icons.Rounded.FitScreen,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFitScreen()
                    }
                )

                DockDivider()

                DockButton(
                    icon = Icons.Rounded.Remove,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onZoomOut()
                    }
                )

                // Zoom display
                Text(
                    text = "$zoomPercent%",
                    color = TextMain,
                    fontSize = 13.sp,
                    modifier = Modifier.widthIn(min = 48.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                DockButton(
                    icon = Icons.Rounded.Add,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onZoomIn()
                    }
                )

                DockDivider()

                DockButton(
                    icon = Icons.Rounded.Undo,
                    enabled = canUndo,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUndo()
                    }
                )

                DockButton(
                    icon = Icons.Rounded.Straighten,
                    isActive = showRuler,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleRuler()
                    }
                )

                DockButton(
                    icon = Icons.Rounded.PhotoCamera,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCameraMode()
                    }
                )

                DockDivider()

                // Settings pill button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEBEBF5)) // Apple-style light gray/white for contrast
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DockButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFFEBEBF5) else Color.Transparent
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> TextDim
                isActive -> Color.Black
                else     -> TextMain
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
            .height(20.dp)
            .background(BorderLight)
    )
}

package com.gridraw.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.viewmodel.ToastType
import com.gridraw.app.ui.theme.*

// ──────────────────────────────────────────────────────────────────────────────
// Toast Host — Floating notification
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ToastHost(
    message: String?,
    type: ToastType,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        if (message != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xF0191924))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val (icon, tint) = when (type) {
                    ToastType.SUCCESS -> Pair(Icons.Rounded.CheckCircle, Success)
                    ToastType.ERROR   -> Pair(Icons.Rounded.Error, Danger)
                    ToastType.WARNING -> Pair(Icons.Rounded.Warning, Warning)
                    ToastType.INFO    -> Pair(Icons.Rounded.Info, AccentBlue)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    color = TextMain,
                    fontSize = 14.sp
                )
            }
        }
    }
}

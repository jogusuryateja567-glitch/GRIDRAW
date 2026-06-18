package com.gridraw.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.data.models.Orientation
import com.gridraw.app.data.models.PaperSize
import com.gridraw.app.ui.theme.*

@Composable
fun CropSetupScreen(
    bitmap: Bitmap,
    initialPaperSize: PaperSize,
    initialOrientation: Orientation,
    customWidthMm: Float = 200f,
    customHeightMm: Float = 200f,
    onConfirm: (Bitmap, PaperSize, Orientation) -> Unit,
    onCancel: () -> Unit
) {
    var paperSize by remember { mutableStateOf(initialPaperSize) }
    var orientation by remember { mutableStateOf(initialOrientation) }

    var imgPanX by remember { mutableFloatStateOf(0f) }
    var imgPanY by remember { mutableFloatStateOf(0f) }
    var imgZoom by remember { mutableFloatStateOf(1f) }
    var hasGestured by remember { mutableStateOf(false) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    val hintAlpha by animateFloatAsState(
        targetValue = if (hasGestured) 0f else 1f,
        animationSpec = tween(durationMillis = 600),
        label = "hintAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, "Cancel", tint = TextMuted)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Position Image",
                        color = TextMain,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Pinch · Drag to adjust", color = TextDim, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            // Build output bitmap
                            val (wMm, hMm) = getPaperDimsMm(paperSize, orientation, customWidthMm, customHeightMm)
                            if (wMm <= 0f || hMm <= 0f) {
                                onConfirm(bitmap, paperSize, orientation)
                                return@clickable
                            }
                            val maxDim = minOf(2048, maxOf(bitmap.width, bitmap.height))
                            val outW: Int
                            val outH: Int
                            if (wMm > hMm) {
                                outW = maxDim
                                outH = (maxDim * (hMm / wMm)).toInt().coerceAtLeast(1)
                            } else {
                                outH = maxDim
                                outW = (maxDim * (wMm / hMm)).toInt().coerceAtLeast(1)
                            }
                            val outBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(outBitmap)
                            canvas.drawColor(AndroidColor.WHITE)

                            val cw = containerSize.width.toFloat()
                            val ch = containerSize.height.toFloat()
                            if (cw == 0f || ch == 0f) {
                                onConfirm(bitmap, paperSize, orientation)
                                return@clickable
                            }
                            val aspect = wMm / hMm
                            val screenPaperW: Float
                            val screenPaperH: Float
                            if (cw / ch > aspect) {
                                screenPaperH = ch * 0.82f
                                screenPaperW = screenPaperH * aspect
                            } else {
                                screenPaperW = cw * 0.82f
                                screenPaperH = screenPaperW / aspect
                            }
                            val outScale = outW / screenPaperW
                            val matrix = android.graphics.Matrix()
                            val dx = (outW - bitmap.width * imgZoom * outScale) / 2f + imgPanX * outScale
                            val dy = (outH - bitmap.height * imgZoom * outScale) / 2f + imgPanY * outScale
                            matrix.postScale(imgZoom * outScale, imgZoom * outScale)
                            matrix.postTranslate(dx, dy)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG).also { it.isFilterBitmap = true }
                            canvas.drawBitmap(bitmap, matrix, paint)
                            onConfirm(outBitmap, paperSize, orientation)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, "Confirm", tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            }

            // ── Crop Viewport ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { containerSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            hasGestured = true
                            imgZoom = (imgZoom * gestureZoom).coerceIn(0.1f, 10f)
                            imgPanX += pan.x
                            imgPanY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (containerSize.width > 0 && containerSize.height > 0) {
                    val cw = containerSize.width.toFloat()
                    val ch = containerSize.height.toFloat()

                    val (wMm, hMm) = getPaperDimsMm(paperSize, orientation, customWidthMm, customHeightMm)
                    val aspect = if (hMm > 0f) wMm / hMm else 1f

                    val paperW: Float
                    val paperH: Float
                    if (cw / ch > aspect) {
                        paperH = ch * 0.82f
                        paperW = paperH * aspect
                    } else {
                        paperW = cw * 0.82f
                        paperH = paperW / aspect
                    }

                    with(LocalDensity.current) {
                        Box(
                            modifier = Modifier
                                .width(paperW.toDp())
                                .height(paperH.toDp())
                                .shadow(20.dp, RoundedCornerShape(2.dp))
                                .background(Color.White)
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            // Image
                            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                withTransform({
                                    translate(left = size.width / 2f + imgPanX, top = size.height / 2f + imgPanY)
                                    scale(imgZoom, imgZoom)
                                    translate(left = -bitmap.width / 2f, top = -bitmap.height / 2f)
                                }) {
                                    drawImage(bitmap.asImageBitmap(), topLeft = Offset.Zero)
                                }
                            }

                            // Grid preview (4×4)
                            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                val cellW = size.width / 4f
                                val cellH = size.height / 4f
                                val lineColor = Color(0xFF5B8DFF).copy(alpha = 0.4f)
                                for (i in 1..3) {
                                    drawLine(lineColor, Offset(cellW * i, 0f), Offset(cellW * i, size.height), 1.5f)
                                    drawLine(lineColor, Offset(0f, cellH * i), Offset(size.width, cellH * i), 1.5f)
                                }
                            }

                            // Corner markers
                            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                                val len = 22.dp.toPx()
                                val thick = 3.5f
                                val cc = Color(0xFF5B8DFF)
                                drawLine(cc, Offset(0f, 0f), Offset(len, 0f), thick)
                                drawLine(cc, Offset(0f, 0f), Offset(0f, len), thick)
                                drawLine(cc, Offset(size.width, 0f), Offset(size.width - len, 0f), thick)
                                drawLine(cc, Offset(size.width, 0f), Offset(size.width, len), thick)
                                drawLine(cc, Offset(0f, size.height), Offset(len, size.height), thick)
                                drawLine(cc, Offset(0f, size.height), Offset(0f, size.height - len), thick)
                                drawLine(cc, Offset(size.width, size.height), Offset(size.width - len, size.height), thick)
                                drawLine(cc, Offset(size.width, size.height), Offset(size.width, size.height - len), thick)
                            }
                        }
                    }

                    // Zoom badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "${String.format("%.1f", imgZoom)}×",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Pinch hint
                    if (hintAlpha > 0.01f) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 20.dp)
                                .graphicsLayer { alpha = hintAlpha }
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.ZoomIn, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Text("Pinch to zoom  ·  Drag to position", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ── Paper & Orientation Controls ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111113))
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    "PAPER SIZE",
                    color = TextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaperSize.A4, PaperSize.A3, PaperSize.A5, PaperSize.LETTER).forEach { size ->
                        val selected = paperSize == size
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) Color.White else Color(0xFF1E1E20),
                            animationSpec = tween(200),
                            label = "paperBg"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (selected) Color.Black else TextMuted,
                            animationSpec = tween(200),
                            label = "paperText"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, if (selected) Color.Transparent else Color(0xFF2C2C2E), RoundedCornerShape(10.dp))
                                .clickable { paperSize = size }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                size.name,
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(Orientation.PORTRAIT, Orientation.LANDSCAPE).forEach { ori ->
                        val selected = orientation == ori
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) Color.White else Color(0xFF1E1E20),
                            animationSpec = tween(200),
                            label = "oriBg"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, if (selected) Color.Transparent else Color(0xFF2C2C2E), RoundedCornerShape(10.dp))
                                .clickable { orientation = ori }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (ori == Orientation.PORTRAIT) "Portrait" else "Landscape",
                                color = if (selected) Color.Black else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getPaperDimsMm(
    paperSize: PaperSize,
    orientation: Orientation,
    customW: Float,
    customH: Float
): Pair<Float, Float> {
    return if (paperSize == PaperSize.CUSTOM) {
        Pair(customW, customH)
    } else if (orientation == Orientation.LANDSCAPE) {
        Pair(paperSize.heightMm, paperSize.widthMm)
    } else {
        Pair(paperSize.widthMm, paperSize.heightMm)
    }
}

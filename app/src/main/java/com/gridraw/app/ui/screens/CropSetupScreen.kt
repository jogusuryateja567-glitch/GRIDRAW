package com.gridraw.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
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

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, "Cancel", tint = TextMain)
                }
                Text("Adjust Image to Paper", color = TextMain, fontSize = 18.sp)
                IconButton(onClick = {
                    // Generate final cropped bitmap
                    val (wMm, hMm) = if (paperSize == PaperSize.CUSTOM) {
                        Pair(customWidthMm, customHeightMm)
                    } else if (orientation == Orientation.LANDSCAPE) {
                        Pair(paperSize.heightMm, paperSize.widthMm)
                    } else {
                        Pair(paperSize.widthMm, paperSize.heightMm)
                    }
                    // Create a high-res bitmap for the final output matching paper aspect ratio
                    // Clamp max dimension to 2048 to prevent OOM
                    val maxDim = minOf(2048, maxOf(bitmap.width, bitmap.height))
                    val outW: Int
                    val outH: Int
                    if (wMm > hMm) {
                        outW = maxDim
                        outH = (maxDim * (hMm / wMm)).toInt()
                    } else {
                        outH = maxDim
                        outW = (maxDim * (wMm / hMm)).toInt()
                    }
                    
                    val outBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(outBitmap)
                    canvas.drawColor(AndroidColor.WHITE) // Padding is white
                    
                    // We need to map the screen panning/zooming to the final bitmap
                    // Wait, the preview draws paper at some screen scale.
                    // Let's compute screen paper rect
                    val screenPaperW: Float
                    val screenPaperH: Float
                    val cw = containerSize.width.toFloat()
                    val ch = containerSize.height.toFloat()
                    if (cw == 0f || ch == 0f) {
                        onConfirm(bitmap, paperSize, orientation)
                        return@IconButton
                    }

                    val aspect = wMm / hMm
                    if (cw / ch > aspect) {
                        screenPaperH = ch * 0.8f
                        screenPaperW = screenPaperH * aspect
                    } else {
                        screenPaperW = cw * 0.8f
                        screenPaperH = screenPaperW / aspect
                    }

                    // Mapping:
                    // The image is drawn with imgZoom and imgPanX, imgPanY relative to the paper center.
                    // Scale factor from screen paper to output bitmap:
                    val outScale = outW / screenPaperW
                    
                    val matrix = android.graphics.Matrix()
                    // Translate to match pan
                    val dx = (outW - bitmap.width * imgZoom * outScale) / 2f + imgPanX * outScale
                    val dy = (outH - bitmap.height * imgZoom * outScale) / 2f + imgPanY * outScale
                    matrix.postScale(imgZoom * outScale, imgZoom * outScale)
                    matrix.postTranslate(dx, dy)
                    
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    paint.isFilterBitmap = true
                    canvas.drawBitmap(bitmap, matrix, paint)
                    
                    onConfirm(outBitmap, paperSize, orientation)
                }) {
                    Icon(Icons.Rounded.Check, "Confirm", tint = AccentBlue)
                }
            }

            // Crop Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { containerSize = it }
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
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
                    
                    val (wMm, hMm) = if (paperSize == PaperSize.CUSTOM) {
                        Pair(customWidthMm, customHeightMm)
                    } else if (orientation == Orientation.LANDSCAPE) {
                        Pair(paperSize.heightMm, paperSize.widthMm)
                    } else {
                        Pair(paperSize.widthMm, paperSize.heightMm)
                    }
                    val aspect = wMm / hMm
                    
                    val paperW: Float
                    val paperH: Float
                    if (cw / ch > aspect) {
                        paperH = ch * 0.8f
                        paperW = paperH * aspect
                    } else {
                        paperW = cw * 0.8f
                        paperH = paperW / aspect
                    }

                    // The white paper background
                    Box(
                        modifier = Modifier
                            .width(with(androidx.compose.ui.platform.LocalDensity.current) { paperW.toDp() })
                            .height(with(androidx.compose.ui.platform.LocalDensity.current) { paperH.toDp() })
                            .background(Color.White)
                            .border(1.dp, BorderGlass)
                            .clipToBounds()
                    ) {
                        // The image drawn inside the paper
                        ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                            withTransform({
                                translate(left = size.width / 2f + imgPanX, top = size.height / 2f + imgPanY)
                                scale(imgZoom, imgZoom)
                                translate(left = -bitmap.width / 2f, top = -bitmap.height / 2f)
                            }) {
                                drawImage(bitmap.asImageBitmap(), topLeft = Offset.Zero)
                            }
                        }
                    }
                }
            }

            // Paper Size Selection Bottom Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgPanel)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Text("Select Paper Size", color = TextMain, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val sizes = listOf(PaperSize.A4, PaperSize.A3, PaperSize.A5)
                    sizes.forEach { size ->
                        Button(
                            onClick = { paperSize = size },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paperSize == size) AccentBlue else Color(0xFF2C2C2E)
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(size.name, color = if (paperSize == size) Color.White else TextMain)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { orientation = Orientation.PORTRAIT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orientation == Orientation.PORTRAIT) AccentBlue else Color(0xFF2C2C2E)
                        ),
                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Portrait")
                    }
                    Button(
                        onClick = { orientation = Orientation.LANDSCAPE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (orientation == Orientation.LANDSCAPE) AccentBlue else Color(0xFF2C2C2E)
                        ),
                        modifier = Modifier.weight(1f).padding(start = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Landscape")
                    }
                }
            }
        }
    }
}

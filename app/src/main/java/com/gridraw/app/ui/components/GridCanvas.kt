package com.gridraw.app.ui.components

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import com.gridraw.app.data.models.*
import com.gridraw.app.ui.theme.*

// ──────────────────────────────────────────────────────────────────────────────
// GridCanvas — Core Compose Canvas Renderer
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    sourceBitmap: Bitmap?,
    filters: ImageFilters,
    grid: GridConfig,
    paperSize: PaperSize,
    orientation: Orientation,
    customWidthMm: Float,
    customHeightMm: Float,
    ppi: Float,
    zoom: Float,
    panX: Float,
    panY: Float,
    showRuler: Boolean = false,
    isCameraMode: Boolean = false,
    rulerP1: Pair<Float, Float>? = null,
    rulerP2: Pair<Float, Float>? = null,
    cameraGridOpacity: Float = 0.7f,
    cameraImageOpacity: Float = 0.5f,
) {
    val ppMm = ppi / 25.4f
    val (rawW, rawH) = when (paperSize) {
        PaperSize.CUSTOM -> Pair(customWidthMm, customHeightMm)
        else -> {
            val base = Pair(paperSize.widthMm, paperSize.heightMm)
            if (orientation == Orientation.LANDSCAPE) Pair(base.second, base.first)
            else base
        }
    }
    val canvasW = (rawW * ppMm).coerceAtLeast(100f)
    val canvasH = (rawH * ppMm).coerceAtLeast(100f)

    // Build Android Paint with color matrix for filters
    val imagePaint = remember(filters) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val cm = ColorMatrix()

        val brightness = filters.brightness / 100f
        val contrast = filters.contrast / 100f
        val translate = 128f * (1f - contrast) + 128f * (brightness - 1f)
        cm.set(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))

        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(filters.saturation / 100f)
        cm.postConcat(satMatrix)

        if (filters.grayscale) {
            val gcm = ColorMatrix()
            gcm.setSaturation(0f)
            cm.postConcat(gcm)
        }
        p.colorFilter = ColorMatrixColorFilter(cm)
        p
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        withTransform({
            translate(left = panX, top = panY)
            scale(zoom, zoom, pivot = Offset.Zero)
        }) {
            // ── Paper Background ──────────────────────────────────────────────
            drawRect(
                color = if (isCameraMode) Color.Transparent else Color.White,
                topLeft = Offset.Zero,
                size = Size(canvasW, canvasH)
            )

            // ── Image ─────────────────────────────────────────────────────────
            if (sourceBitmap != null) {
                val imgW = sourceBitmap.width.toFloat()
                val imgH = sourceBitmap.height.toFloat()
                val scale = maxOf(canvasW / imgW, canvasH / imgH)
                val dx = (canvasW - imgW * scale) / 2f
                val dy = (canvasH - imgH * scale) / 2f

                withTransform({
                    translate(left = dx, top = dy)
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    val paintToUse = if (isCameraMode) {
                        val camPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                        camPaint.alpha = (255 * cameraImageOpacity).toInt().coerceIn(0, 255)
                        camPaint.colorFilter = imagePaint.colorFilter
                        camPaint
                    } else {
                        imagePaint
                    }
                    drawContext.canvas.nativeCanvas.drawBitmap(sourceBitmap, 0f, 0f, paintToUse)
                }
            }

            // ── Grid Overlay ──────────────────────────────────────────────────
            drawGridOverlay(
                canvasW = canvasW,
                canvasH = canvasH,
                grid = grid,
                ppMm = ppMm,
                isCameraMode = isCameraMode,
                cameraGridOpacity = cameraGridOpacity
            )

            // ── Ruler ─────────────────────────────────────────────────────────
            if (showRuler && rulerP1 != null) {
                val p1 = Offset(rulerP1.first, rulerP1.second)
                drawCircle(Warning, radius = 8f / zoom, center = p1)
                drawCircle(Color.Black.copy(alpha = 0.4f), radius = 5f / zoom, center = p1)

                if (rulerP2 != null) {
                    val p2 = Offset(rulerP2.first, rulerP2.second)
                    drawLine(
                        color = Warning,
                        start = p1,
                        end = p2,
                        strokeWidth = 2.5f / zoom,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                    drawCircle(Warning, radius = 8f / zoom, center = p2)
                    drawCircle(Color.Black.copy(alpha = 0.4f), radius = 5f / zoom, center = p2)
                }
            }

            // ── Paper Border ──────────────────────────────────────────────────
            drawRect(
                color = Color.Black.copy(alpha = 0.3f),
                topLeft = Offset(4f / zoom, 4f / zoom),
                size = Size(canvasW, canvasH),
            )
            drawRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset.Zero,
                size = Size(canvasW, canvasH),
                style = Stroke(width = 1.5f / zoom)
            )
        }
    }
}

private fun DrawScope.drawGridOverlay(
    canvasW: Float,
    canvasH: Float,
    grid: GridConfig,
    ppMm: Float,
    isCameraMode: Boolean = false,
    cameraGridOpacity: Float = 0.7f
) {
    if (!grid.enabled) return
    val boxPx = grid.sizeMm * ppMm
    if (boxPx < 3f) return

    val rawColor = grid.colorHex
    val r = ((rawColor shr 16) and 0xFF) / 255f
    val g = ((rawColor shr 8) and 0xFF) / 255f
    val b = (rawColor and 0xFF) / 255f

    val opacityToUse = if (isCameraMode) cameraGridOpacity else (grid.opacityPct / 100f)
    val gridColor = Color(r, g, b, opacityToUse)

    var x = 0f
    while (x <= canvasW + 0.5f) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, canvasH), grid.thickness)
        x += boxPx
    }
    var y = 0f
    while (y <= canvasH + 0.5f) {
        drawLine(gridColor, Offset(0f, y), Offset(canvasW, y), grid.thickness)
        y += boxPx
    }

    if (grid.showDiagonals) {
        val diagColor = gridColor.copy(alpha = gridColor.alpha * 0.5f)
        var cx = 0f
        while (cx < canvasW) {
            var cy = 0f
            while (cy < canvasH) {
                drawLine(diagColor, Offset(cx, cy), Offset(cx + boxPx, cy + boxPx), grid.thickness)
                drawLine(diagColor, Offset(cx + boxPx, cy), Offset(cx, cy + boxPx), grid.thickness)
                cy += boxPx
            }
            cx += boxPx
        }
    }

    if (grid.showThirds) {
        val thirdsColor = Color(1f, 0.298f, 0.416f, 0.85f)
        val thirdsStroke = grid.thickness * 2.5f
        drawLine(thirdsColor, Offset(canvasW / 3f, 0f), Offset(canvasW / 3f, canvasH), thirdsStroke)
        drawLine(thirdsColor, Offset(canvasW * 2f / 3f, 0f), Offset(canvasW * 2f / 3f, canvasH), thirdsStroke)
        drawLine(thirdsColor, Offset(0f, canvasH / 3f), Offset(canvasW, canvasH / 3f), thirdsStroke)
        drawLine(thirdsColor, Offset(0f, canvasH * 2f / 3f), Offset(canvasW, canvasH * 2f / 3f), thirdsStroke)
    }

    if (grid.showSymmetryH) {
        val symColor = Color(0f, 0.906f, 0.463f, 0.85f)
        drawLine(symColor, Offset(0f, canvasH / 2f), Offset(canvasW, canvasH / 2f), grid.thickness * 2f)
    }

    if (grid.showSymmetryV) {
        val symColor = Color(0f, 0.906f, 0.463f, 0.85f)
        drawLine(symColor, Offset(canvasW / 2f, 0f), Offset(canvasW / 2f, canvasH), grid.thickness * 2f)
    }

    if (grid.showSymmetryRadial) {
        val cx = canvasW / 2f
        val cy = canvasH / 2f
        val radius = maxOf(canvasW, canvasH)
        val radColor = Color(0.596f, 0.42f, 1f, 0.6f)
        val segments = grid.symmetrySegments.coerceIn(2, 36)
        for (i in 0 until segments) {
            val angle = Math.PI * 2.0 * i / segments
            val ex = cx + (radius * Math.cos(angle)).toFloat()
            val ey = cy + (radius * Math.sin(angle)).toFloat()
            drawLine(radColor, Offset(cx, cy), Offset(ex, ey), grid.thickness)
        }
    }

    if (grid.showLabels && boxPx >= 20f) {
        val labelFontSize = (boxPx * 0.28f).coerceIn(10f, 40f)
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = (rawColor.toInt() and 0x00FFFFFF) or (0xFF shl 24)
                textSize = labelFontSize
                isFakeBoldText = true
            }
            val strokePaint = android.graphics.Paint(textPaint).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.argb(180, 0, 0, 0)
            }

            var col = 1
            var lx = 0f
            while (lx < canvasW - 10) {
                val txt = col.toString()
                drawText(txt, lx + 5f, labelFontSize + 5f, strokePaint)
                drawText(txt, lx + 5f, labelFontSize + 5f, textPaint)
                lx += boxPx
                col++
            }

            var row = 0
            var ly = 0f
            while (ly < canvasH - 10) {
                val label = buildRowLabel(row)
                val textY = ly + labelFontSize * 2.0f + 5f
                if (textY < canvasH) {
                    drawText(label, 5f, textY, strokePaint)
                    drawText(label, 5f, textY, textPaint)
                }
                ly += boxPx
                row++
            }
        }
    }
}

private fun buildRowLabel(n: Int): String {
    var num = n
    var label = ""
    do {
        label = ('A' + (num % 26)).toString() + label
        num = num / 26 - 1
    } while (num >= 0)
    return label
}
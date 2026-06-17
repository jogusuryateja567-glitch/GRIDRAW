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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    rulerP1: Pair<Float, Float>? = null,
    rulerP2: Pair<Float, Float>? = null,
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

        // Brightness + Contrast combined
        val brightness = filters.brightness / 100f
        val contrast = filters.contrast / 100f
        val translate = 128f * (1f - contrast) + 128f * (brightness - 1f)
        cm.set(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        if (filters.grayscale) {
            val gcm = ColorMatrix()
            gcm.setSaturation(0f)
            cm.postConcat(gcm)
        }
        p.colorFilter = ColorMatrixColorFilter(cm)
        p
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Apply viewport transform
        withTransform({
            translate(left = panX, top = panY)
            scale(zoom, zoom, pivot = Offset.Zero)
        }) {
            // ── Paper Background ──────────────────────────────────────────────
            drawRect(
                color = Color.White,
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

                drawContext.canvas.nativeCanvas.apply {
                    save()
                    clipRect(0f, 0f, canvasW, canvasH)
                    translate(dx, dy)
                    scale(scale, scale)
                    drawBitmap(sourceBitmap, 0f, 0f, imagePaint)
                    restore()
                }
            }

            // ── Grid Overlay ──────────────────────────────────────────────────
            drawGridOverlay(
                canvasW = canvasW,
                canvasH = canvasH,
                grid = grid,
                ppMm = ppMm
            )

            // ── Ruler ─────────────────────────────────────────────────────────
            if (showRuler && rulerP1 != null && rulerP2 != null) {
                val p1 = Offset(rulerP1.first, rulerP1.second)
                val p2 = Offset(rulerP2.first, rulerP2.second)
                drawLine(
                    color = Warning,
                    start = p1,
                    end = p2,
                    strokeWidth = 2f / zoom,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                )
                drawCircle(Warning, radius = 6f / zoom, center = p1)
                drawCircle(Warning, radius = 6f / zoom, center = p2)
            }

            // ── Paper Shadow & Border ─────────────────────────────────────────
            drawRect(
                color = Color.White.copy(alpha = 0.1f),
                topLeft = Offset.Zero,
                size = Size(canvasW, canvasH),
                style = Stroke(width = 1f / zoom)
            )
        }
    }
}

private fun DrawScope.drawGridOverlay(
    canvasW: Float,
    canvasH: Float,
    grid: GridConfig,
    ppMm: Float
) {
    if (!grid.enabled) return
    val boxPx = grid.sizeMm * ppMm
    if (boxPx < 5f) return

    val rawColor = grid.colorHex
    val r = ((rawColor shr 16) and 0xFF) / 255f
    val g = ((rawColor shr 8) and 0xFF) / 255f
    val b = (rawColor and 0xFF) / 255f
    val gridColor = Color(r, g, b, grid.opacityPct / 100f)

    // Main grid lines
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

    // Diagonals
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

    // Rule of Thirds
    if (grid.showThirds) {
        val thirdsColor = Color(1f, 0.298f, 0.416f, 0.85f)
        val thirdsStroke = grid.thickness * 2.5f
        drawLine(thirdsColor, Offset(canvasW / 3f, 0f), Offset(canvasW / 3f, canvasH), thirdsStroke)
        drawLine(thirdsColor, Offset(canvasW * 2f / 3f, 0f), Offset(canvasW * 2f / 3f, canvasH), thirdsStroke)
        drawLine(thirdsColor, Offset(0f, canvasH / 3f), Offset(canvasW, canvasH / 3f), thirdsStroke)
        drawLine(thirdsColor, Offset(0f, canvasH * 2f / 3f), Offset(canvasW, canvasH * 2f / 3f), thirdsStroke)
    }

    // Symmetry H
    if (grid.showSymmetryH) {
        val symColor = Color(0f, 0.906f, 0.463f, 0.85f)
        drawLine(symColor, Offset(0f, canvasH / 2f), Offset(canvasW, canvasH / 2f), grid.thickness * 2f)
    }

    // Symmetry V
    if (grid.showSymmetryV) {
        val symColor = Color(0f, 0.906f, 0.463f, 0.85f)
        drawLine(symColor, Offset(canvasW / 2f, 0f), Offset(canvasW / 2f, canvasH), grid.thickness * 2f)
    }

    // Radial Symmetry
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

    // Labels
    if (grid.showLabels) {
        val labelFontSize = (boxPx * 0.28f).coerceIn(10f, 36f)
        drawContext.canvas.nativeCanvas.apply {
            val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = (rawColor.toInt() and 0x00FFFFFF) or (0xFF shl 24)
                textSize = labelFontSize
                isFakeBoldText = true
            }
            val strokePaint = android.graphics.Paint(textPaint).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.argb(180, 255, 255, 255)
            }

            var col = 1
            var lx = 0f
            while (lx < canvasW - 10) {
                val txt = col.toString()
                drawText(txt, lx + 4f, labelFontSize + 4f, strokePaint)
                drawText(txt, lx + 4f, labelFontSize + 4f, textPaint)
                lx += boxPx
                col++
            }

            var row = 0
            var ly = 0f
            while (ly < canvasH - 10) {
                val label = buildRowLabel(row)
                val yPos = ly + labelFontSize * 2.2f
                drawText(label, 4f, yPos, strokePaint)
                drawText(label, 4f, yPos, textPaint)
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

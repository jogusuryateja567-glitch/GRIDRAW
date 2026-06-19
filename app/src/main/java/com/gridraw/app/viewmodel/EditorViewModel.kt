package com.gridraw.app.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridraw.app.GridRawApplication
import com.gridraw.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────────────────
// Editor State
// ─────────────────────────────────────────────────────────────────────────────

data class EditorState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Processing…",
    val hasImage: Boolean = false,
    val sourceBitmap: Bitmap? = null,
    val pendingCropBitmap: Bitmap? = null,
    val pendingImageUri: String? = null, // Track source URI for project save

    val paperSize: PaperSize = PaperSize.A4,
    val orientation: Orientation = Orientation.PORTRAIT,
    val customWidthMm: Float = 200f,
    val customHeightMm: Float = 200f,

    val cropState: CropState = CropState(),
    val filters: ImageFilters = ImageFilters(),
    val grid: GridConfig = GridConfig(),
    val ppi: Float = 96f,

    // Viewport
    val viewportZoom: Float = 1f,
    val viewportOffsetX: Float = 0f,
    val viewportOffsetY: Float = 0f,

    // UI
    val isPanelOpen: Boolean = false,
    val activeTab: Int = 0,
    val isCameraMode: Boolean = false,
    val showRuler: Boolean = false,
    val rulerPoint1: Pair<Float, Float>? = null,
    val rulerPoint2: Pair<Float, Float>? = null,
    val showPaletteSheet: Boolean = false,
    val extractedPalette: List<Long> = emptyList(),
    val toastMessage: String? = null,
    val toastType: ToastType = ToastType.INFO,
    val canUndo: Boolean = false,
    // AR Camera overlay controls (independent of normal grid settings)
    val cameraGridOpacity: Float = 0.7f,
    val cameraImageOpacity: Float = 0.5f
)

enum class ToastType { INFO, SUCCESS, WARNING, ERROR }

data class HistorySnapshot(
    val paperSize: PaperSize,
    val orientation: Orientation,
    val customWidthMm: Float,
    val customHeightMm: Float,
    val cropState: CropState,
    val filters: ImageFilters,
    val grid: GridConfig,
    val ppi: Float
)

// ─────────────────────────────────────────────────────────────────────────────
// EditorViewModel
// ─────────────────────────────────────────────────────────────────────────────

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private val db = (application as GridRawApplication).database
    private val dao = db.projectDao()

    private val historyStack = ArrayDeque<HistorySnapshot>()
    private val maxHistory = 20

    // Debounce jobs for high-frequency updates (sliders)
    private var filterDebounceJob: Job? = null
    private var gridDebounceJob: Job? = null
    private var toastJob: Job? = null

    companion object {
        private const val MAX_BITMAP_DIMENSION = 2048
    }

    init {
        val displayDpi = application.resources.displayMetrics.xdpi
        _state.update { it.copy(ppi = displayDpi.coerceIn(72f, 600f)) }
    }

    // ── Image Loading ────────────────────────────────────────────────────────

    /**
     * Load image from URI with automatic downsampling to prevent OOM crashes.
     * Large photos (e.g. 4000×3000) are decoded at reduced resolution.
     */
    fun loadImageFromUri(context: Context, uri: Uri, onLoaded: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Loading image…") }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    // Step 1: Decode bounds only to calculate sample size
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                    }

                    val origWidth = options.outWidth
                    val origHeight = options.outHeight

                    if (origWidth <= 0 || origHeight <= 0) return@withContext null

                    // Step 2: Calculate inSampleSize to downsample large images
                    var sampleSize = 1
                    val maxDim = maxOf(origWidth, origHeight)
                    if (maxDim > MAX_BITMAP_DIMENSION) {
                        while (maxDim / sampleSize > MAX_BITMAP_DIMENSION * 2) {
                            sampleSize *= 2
                        }
                    }

                    // Step 3: Decode with downsampling
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, decodeOptions)
                    } ?: return@withContext null

                    // Step 4: If still larger than max, scale down precisely
                    val longestEdge = maxOf(decoded.width, decoded.height)
                    if (longestEdge > MAX_BITMAP_DIMENSION) {
                        val scale = MAX_BITMAP_DIMENSION.toFloat() / longestEdge
                        val newW = (decoded.width * scale).toInt().coerceAtLeast(1)
                        val newH = (decoded.height * scale).toInt().coerceAtLeast(1)
                        val scaled = Bitmap.createScaledBitmap(decoded, newW, newH, true)
                        if (scaled !== decoded) decoded.recycle()
                        scaled
                    } else {
                        decoded
                    }
                }
                if (bitmap != null) {
                    _state.update {
                        it.copy(
                            pendingCropBitmap = bitmap,
                            pendingImageUri = uri.toString(),
                            isLoading = false
                        )
                    }
                    withContext(Dispatchers.Main) { onLoaded(true) }
                } else {
                    showToast("Failed to load image", ToastType.ERROR)
                    _state.update { it.copy(isLoading = false) }
                    withContext(Dispatchers.Main) { onLoaded(false) }
                }
            } catch (e: Exception) {
                showToast("Error: ${e.localizedMessage}", ToastType.ERROR)
                _state.update { it.copy(isLoading = false) }
                withContext(Dispatchers.Main) { onLoaded(false) }
            }
        }
    }

    fun applyCrop(context: Context, bitmap: Bitmap, paperSize: PaperSize, orientation: Orientation, sourceUri: String? = null) {
        val uriToStore = sourceUri ?: _state.value.pendingImageUri
        _state.update {
            it.copy(
                sourceBitmap = bitmap,
                pendingCropBitmap = null,
                pendingImageUri = null,
                hasImage = true,
                paperSize = paperSize,
                orientation = orientation,
                cropState = CropState(scale = 1f)
            )
        }
        pushHistory()
        // Generate descriptive project name from metadata
        val gridSizeMm = _state.value.grid.sizeMm.toInt()
        val projectName = "${paperSize.name} · ${orientation.name.lowercase().replaceFirstChar { it.uppercase() }} · ${gridSizeMm}mm"
        saveCurrentProject(context, projectName, uriToStore)
    }

    // ── Canvas Setup ─────────────────────────────────────────────────────────

    fun setPaperSize(size: PaperSize) {
        _state.update { it.copy(paperSize = size) }
        pushHistory()
    }

    fun toggleOrientation() {
        _state.update {
            it.copy(orientation = if (it.orientation == Orientation.PORTRAIT)
                Orientation.LANDSCAPE else Orientation.PORTRAIT)
        }
        pushHistory()
    }

    fun setCustomDimensions(widthMm: Float, heightMm: Float) {
        _state.update { it.copy(customWidthMm = widthMm, customHeightMm = heightMm) }
        pushHistory()
    }

    fun getCanvasSizePx(): Pair<Int, Int> {
        val st = _state.value
        val ppMm = st.ppi / 25.4f
        val (wMm, hMm) = when (st.paperSize) {
            PaperSize.CUSTOM -> Pair(st.customWidthMm, st.customHeightMm)
            else -> {
                val base = Pair(st.paperSize.widthMm, st.paperSize.heightMm)
                if (st.orientation == Orientation.LANDSCAPE) Pair(base.second, base.first)
                else base
            }
        }
        return Pair(
            (wMm * ppMm).toInt().coerceAtLeast(100),
            (hMm * ppMm).toInt().coerceAtLeast(100)
        )
    }

    // ── Auto Fit Viewport ────────────────────────────────────────────────────

    /**
     * Calculate zoom and pan to fit the canvas to the available screen area.
     * Called after image load to ensure the full image is visible.
     */
    fun autoFitViewport(screenWidth: Float, screenHeight: Float) {
        val st = _state.value
        val ppMm = st.ppi / 25.4f
        val (rawW, rawH) = when (st.paperSize) {
            PaperSize.CUSTOM -> Pair(st.customWidthMm, st.customHeightMm)
            else -> {
                val base = Pair(st.paperSize.widthMm, st.paperSize.heightMm)
                if (st.orientation == Orientation.LANDSCAPE) Pair(base.second, base.first)
                else base
            }
        }
        val canvasW = (rawW * ppMm).coerceAtLeast(100f)
        val canvasH = (rawH * ppMm).coerceAtLeast(100f)

        // Fit with 8% padding
        val zoomX = screenWidth * 0.92f / canvasW
        val zoomY = screenHeight * 0.92f / canvasH
        val fitZoom = minOf(zoomX, zoomY).coerceIn(0.05f, 10f)

        // Center the canvas
        val panX = (screenWidth - canvasW * fitZoom) / 2f
        val panY = (screenHeight - canvasH * fitZoom) / 2f

        _state.update {
            it.copy(
                viewportZoom = fitZoom,
                viewportOffsetX = panX,
                viewportOffsetY = panY
            )
        }
    }

    // ── Filters (debounced to avoid history spam on slider drag) ─────────────

    fun updateFilters(filters: ImageFilters) {
        _state.update { it.copy(filters = filters) }
        filterDebounceJob?.cancel()
        filterDebounceJob = viewModelScope.launch {
            delay(400)
            pushHistory()
        }
    }

    fun resetFilters() {
        filterDebounceJob?.cancel()
        _state.update { it.copy(filters = ImageFilters()) }
        showToast("Filters reset", ToastType.INFO)
        pushHistory()
    }

    // ── Grid (debounced) ─────────────────────────────────────────────────────

    fun updateGrid(grid: GridConfig) {
        _state.update { it.copy(grid = grid) }
        gridDebounceJob?.cancel()
        gridDebounceJob = viewModelScope.launch {
            delay(400)
            pushHistory()
        }
    }

    // ── Viewport ─────────────────────────────────────────────────────────────

    fun setViewport(zoom: Float, offsetX: Float, offsetY: Float) {
        _state.update { it.copy(viewportZoom = zoom, viewportOffsetX = offsetX, viewportOffsetY = offsetY) }
    }

    fun setPpi(ppi: Float) {
        _state.update { it.copy(ppi = ppi.coerceIn(72f, 600f)) }
        pushHistory()
    }

    // ── UI Control ───────────────────────────────────────────────────────────

    // FIX: was ignoring the tab param and always using 0
    fun openPanel(tab: Int = _state.value.activeTab) {
        _state.update { it.copy(isPanelOpen = true, activeTab = tab) }
    }

    fun closePanel() {
        _state.update { it.copy(isPanelOpen = false) }
    }

    fun setActiveTab(tab: Int) {
        _state.update { it.copy(activeTab = tab) }
    }

    fun toggleCameraMode() {
        _state.update { it.copy(isCameraMode = !it.isCameraMode) }
    }

    fun setCameraGridOpacity(opacity: Float) {
        _state.update { it.copy(cameraGridOpacity = opacity.coerceIn(0f, 1f)) }
    }

    fun setCameraImageOpacity(opacity: Float) {
        _state.update { it.copy(cameraImageOpacity = opacity.coerceIn(0f, 1f)) }
    }

    fun toggleRuler() {
        _state.update { it.copy(showRuler = !it.showRuler, rulerPoint1 = null, rulerPoint2 = null) }
    }

    fun setRulerPoint(x: Float, y: Float) {
        val st = _state.value
        when {
            st.rulerPoint1 == null -> _state.update { it.copy(rulerPoint1 = Pair(x, y)) }
            st.rulerPoint2 == null -> _state.update { it.copy(rulerPoint2 = Pair(x, y)) }
            else -> _state.update { it.copy(rulerPoint1 = Pair(x, y), rulerPoint2 = null) }
        }
    }

    // FIX: ruler points are already in canvas space; zoom should not be divided again
    fun getMeasuredDistanceMm(): Float? {
        val st = _state.value
        val p1 = st.rulerPoint1 ?: return null
        val p2 = st.rulerPoint2 ?: return null
        val ppMm = st.ppi / 25.4f
        val dx = (p2.first - p1.first) / ppMm
        val dy = (p2.second - p1.second) / ppMm
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun extractPalette() {
        val bitmap = _state.value.sourceBitmap ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Extracting palette…") }
            val colors = withContext(Dispatchers.Default) {
                try {
                    val palette = androidx.palette.graphics.Palette.from(bitmap).generate()
                    listOfNotNull(
                        palette.vibrantSwatch?.rgb?.toLong()?.or(0xFF000000L),
                        palette.darkVibrantSwatch?.rgb?.toLong()?.or(0xFF000000L),
                        palette.lightVibrantSwatch?.rgb?.toLong()?.or(0xFF000000L),
                        palette.mutedSwatch?.rgb?.toLong()?.or(0xFF000000L),
                        palette.darkMutedSwatch?.rgb?.toLong()?.or(0xFF000000L),
                        palette.lightMutedSwatch?.rgb?.toLong()?.or(0xFF000000L)
                    )
                } catch (e: Exception) { emptyList() }
            }
            _state.update { it.copy(isLoading = false, showPaletteSheet = true, extractedPalette = colors) }
        }
    }

    fun closePaletteSheet() {
        _state.update { it.copy(showPaletteSheet = false) }
    }

    fun showToast(message: String, type: ToastType = ToastType.INFO) {
        toastJob?.cancel()
        _state.update { it.copy(toastMessage = message, toastType = type) }
        toastJob = viewModelScope.launch {
            delay(3000)
            _state.update { it.copy(toastMessage = null) }
        }
    }

    fun dismissToast() {
        toastJob?.cancel()
        _state.update { it.copy(toastMessage = null) }
    }

    // ── Undo ─────────────────────────────────────────────────────────────────

    private fun pushHistory() {
        val st = _state.value
        val snap = HistorySnapshot(
            paperSize = st.paperSize,
            orientation = st.orientation,
            customWidthMm = st.customWidthMm,
            customHeightMm = st.customHeightMm,
            cropState = st.cropState,
            filters = st.filters,
            grid = st.grid,
            ppi = st.ppi
        )
        // Avoid duplicate snapshots
        if (historyStack.lastOrNull() == snap) return
        if (historyStack.size >= maxHistory) historyStack.removeFirst()
        historyStack.addLast(snap)
        _state.update { it.copy(canUndo = historyStack.size > 1) }
    }

    fun undo() {
        if (historyStack.size <= 1) return
        historyStack.removeLast()
        val snap = historyStack.last()
        _state.update {
            it.copy(
                paperSize = snap.paperSize,
                orientation = snap.orientation,
                customWidthMm = snap.customWidthMm,
                customHeightMm = snap.customHeightMm,
                cropState = snap.cropState,
                filters = snap.filters,
                grid = snap.grid,
                ppi = snap.ppi,
                canUndo = historyStack.size > 1
            )
        }
        showToast("Undone", ToastType.INFO)
    }

    // ── Export ───────────────────────────────────────────────────────────────

    fun exportImage(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadingMessage = "Exporting image…") }
            val success = withContext(Dispatchers.IO) {
                try {
                    val st = _state.value
                    val src = st.sourceBitmap ?: return@withContext false
                    val (cw, ch) = getCanvasSizePx()

                    val out = Bitmap.createBitmap(cw, ch, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(out)
                    canvas.drawColor(android.graphics.Color.WHITE)

                    // FIX: match the exact same color matrix logic as GridCanvas
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    val cm = ColorMatrix()
                    val brightness = st.filters.brightness / 100f
                    val contrast = st.filters.contrast / 100f
                    val translate = 128f * (1f - contrast) + 128f * (brightness - 1f)
                    cm.set(floatArrayOf(
                        contrast, 0f, 0f, 0f, translate,
                        0f, contrast, 0f, 0f, translate,
                        0f, 0f, contrast, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    if (st.filters.grayscale) {
                        val gcm = ColorMatrix()
                        gcm.setSaturation(0f)
                        cm.postConcat(gcm)
                    }
                    paint.colorFilter = ColorMatrixColorFilter(cm)

                    val scale = maxOf(cw.toFloat() / src.width, ch.toFloat() / src.height)
                    val dx = (cw - src.width * scale) / 2f
                    val dy = (ch - src.height * scale) / 2f
                    val dst = android.graphics.RectF(dx, dy, dx + src.width * scale, dy + src.height * scale)
                    canvas.drawBitmap(src, null, dst, paint)

                    drawGridOnCanvas(canvas, cw.toFloat(), ch.toFloat(), st.grid, st.ppi)

                    val filename = "GRIDRAW_${System.currentTimeMillis()}.jpg"
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/GRIDRAW")
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let { u ->
                        resolver.openOutputStream(u)?.use { os ->
                            out.compress(Bitmap.CompressFormat.JPEG, 95, os)
                        }
                    }
                    out.recycle()
                    uri != null
                } catch (e: Exception) { false }
            }
            _state.update { it.copy(isLoading = false) }
            withContext(Dispatchers.Main) {
                if (success) showToast("Saved to Gallery!", ToastType.SUCCESS)
                else showToast("Export failed", ToastType.ERROR)
                onComplete(success)
            }
        }
    }

    private fun drawGridOnCanvas(
        canvas: android.graphics.Canvas,
        cw: Float, ch: Float,
        grid: GridConfig,
        ppi: Float
    ) {
        if (!grid.enabled) return
        val ppMm = ppi / 25.4f
        val boxPx = grid.sizeMm * ppMm
        if (boxPx < 5f) return

        // FIX: correct Long-safe color extraction
        val rawColor = grid.colorHex
        val r = ((rawColor shr 16) and 0xFF).toInt()
        val g = ((rawColor shr 8) and 0xFF).toInt()
        val b = (rawColor and 0xFF).toInt()
        val alpha = (grid.opacityPct / 100f * 255).toInt()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = grid.thickness
            color = android.graphics.Color.argb(alpha, r, g, b)
        }

        var x = 0f
        while (x <= cw + 0.5f) { canvas.drawLine(x, 0f, x, ch, paint); x += boxPx }
        var y = 0f
        while (y <= ch + 0.5f) { canvas.drawLine(0f, y, cw, y, paint); y += boxPx }

        if (grid.showDiagonals) {
            val savedAlpha = paint.alpha
            paint.alpha = (savedAlpha * 0.5f).toInt()
            var cx = 0f
            while (cx < cw) {
                var cy = 0f
                while (cy < ch) {
                    canvas.drawLine(cx, cy, cx + boxPx, cy + boxPx, paint)
                    canvas.drawLine(cx + boxPx, cy, cx, cy + boxPx, paint)
                    cy += boxPx
                }
                cx += boxPx
            }
            paint.alpha = savedAlpha
        }

        if (grid.showThirds) {
            paint.color = android.graphics.Color.argb(200, 255, 77, 106)
            paint.strokeWidth = grid.thickness * 2.5f
            canvas.drawLine(cw / 3f, 0f, cw / 3f, ch, paint)
            canvas.drawLine(cw * 2f / 3f, 0f, cw * 2f / 3f, ch, paint)
            canvas.drawLine(0f, ch / 3f, cw, ch / 3f, paint)
            canvas.drawLine(0f, ch * 2f / 3f, cw, ch * 2f / 3f, paint)
        }

        if (grid.showSymmetryH) {
            paint.color = android.graphics.Color.argb(200, 0, 230, 118)
            paint.strokeWidth = grid.thickness * 2f
            canvas.drawLine(0f, ch / 2f, cw, ch / 2f, paint)
        }

        if (grid.showSymmetryV) {
            paint.color = android.graphics.Color.argb(200, 0, 230, 118)
            paint.strokeWidth = grid.thickness * 2f
            canvas.drawLine(cw / 2f, 0f, cw / 2f, ch, paint)
        }

        if (grid.showSymmetryRadial) {
            val cx = cw / 2f; val cy = ch / 2f
            val radius = maxOf(cw, ch)
            paint.color = android.graphics.Color.argb(153, 152, 107, 255)
            paint.strokeWidth = grid.thickness
            val segments = grid.symmetrySegments.coerceIn(2, 36)
            for (i in 0 until segments) {
                val angle = Math.PI * 2.0 * i / segments
                canvas.drawLine(cx, cy,
                    cx + (radius * Math.cos(angle)).toFloat(),
                    cy + (radius * Math.sin(angle)).toFloat(), paint)
            }
        }

        if (grid.showLabels) {
            val fontSize = (boxPx * 0.28f).coerceIn(10f, 36f)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(255, r, g, b)
                textSize = fontSize
                isFakeBoldText = true
            }
            val strokePaint = Paint(textPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.argb(180, 255, 255, 255)
            }
            var col = 1; var lx = 0f
            while (lx < cw - 10) {
                val txt = col.toString()
                canvas.drawText(txt, lx + 4f, fontSize + 4f, strokePaint)
                canvas.drawText(txt, lx + 4f, fontSize + 4f, textPaint)
                lx += boxPx; col++
            }
            var row = 0; var ly = 0f
            while (ly < ch - 10) {
                val label = buildRowLabel(row)
                val yPos = ly + fontSize * 2.2f
                canvas.drawText(label, 4f, yPos, strokePaint)
                canvas.drawText(label, 4f, yPos, textPaint)
                ly += boxPx; row++
            }
        }
    }

    // FIX: unified with GridCanvas version
    private fun buildRowLabel(n: Int): String {
        var num = n; var label = ""
        do { label = ('A' + (num % 26)).toString() + label; num = num / 26 - 1 } while (num >= 0)
        return label
    }

    // ── Project Save/Load ────────────────────────────────────────────────────

    fun saveCurrentProject(context: Context, name: String, imageUri: String?) {
        viewModelScope.launch {
            val st = _state.value
            val project = com.gridraw.app.data.models.Project(
                name = name,
                updatedAt = System.currentTimeMillis(),
                imageUri = imageUri,
                paperSize = st.paperSize.name,
                orientation = st.orientation.name,
                customWidthMm = st.customWidthMm,
                customHeightMm = st.customHeightMm,
                ppi = st.ppi
            )
            dao.insertProject(project)
        }
    }
}
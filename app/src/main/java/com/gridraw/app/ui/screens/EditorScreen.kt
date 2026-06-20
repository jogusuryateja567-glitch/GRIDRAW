package com.gridraw.app.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.components.*
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.EditorViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

// ─────────────────────────────────────────────────────────────────────────────
// EditorScreen — Main Canvas + Controls
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current

    var zoom by remember { mutableFloatStateOf(state.viewportZoom.coerceAtLeast(0.05f)) }
    var panX by remember { mutableFloatStateOf(state.viewportOffsetX) }
    var panY by remember { mutableFloatStateOf(state.viewportOffsetY) }

    // FIX: key on sourceBitmap identity so auto-fit re-runs on every new image load
    var autoFitKey by remember { mutableStateOf<Any?>(null) }

    BackHandler { onNavigateBack() }

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImageFromUri(context, it) }
    }

    val hazeState = remember { HazeState() }
    val dragSafeMargin = 200f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // ── Layer 1: Canvas (haze source)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenWidth = constraints.maxWidth.toFloat()
                val screenHeight = constraints.maxHeight.toFloat()

                val ppMm = state.ppi / 25.4f
                val (rawW, rawH) = when (state.paperSize) {
                    com.gridraw.app.data.models.PaperSize.CUSTOM ->
                        Pair(state.customWidthMm, state.customHeightMm)
                    else -> {
                        val base = Pair(state.paperSize.widthMm, state.paperSize.heightMm)
                        if (state.orientation == com.gridraw.app.data.models.Orientation.LANDSCAPE)
                            Pair(base.second, base.first)
                        else base
                    }
                }
                val canvasW = (rawW * ppMm).coerceAtLeast(100f)
                val canvasH = (rawH * ppMm).coerceAtLeast(100f)

                // FIX: key on sourceBitmap so new image always triggers re-fit
                LaunchedEffect(state.sourceBitmap, state.paperSize, state.orientation) {
                    val bmp = state.sourceBitmap ?: return@LaunchedEffect
                    val key = Triple(bmp, state.paperSize, state.orientation)
                    if (key != autoFitKey && screenWidth > 0f && screenHeight > 0f) {
                        viewModel.autoFitViewport(screenWidth, screenHeight)
                        val newState = viewModel.state.value
                        zoom = newState.viewportZoom
                        panX = newState.viewportOffsetX
                        panY = newState.viewportOffsetY
                        autoFitKey = key
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.paperSize, state.orientation, state.ppi) {
                            detectTransformGestures { centroid, pan, gestureZoom, _ ->
                                val minZoom = minOf(screenWidth * 0.90f / canvasW, screenHeight * 0.85f / canvasH).coerceIn(0.05f, 10f)
                                val newZoom = (zoom * gestureZoom).coerceIn(minZoom, 10f)
                                val zoomFactor = newZoom / zoom

                                var newPanX = centroid.x - (centroid.x - panX) * zoomFactor + pan.x
                                var newPanY = centroid.y - (centroid.y - panY) * zoomFactor + pan.y

                                val scaledW = canvasW * newZoom
                                val scaledH = canvasH * newZoom

                                newPanX = if (scaledW >= screenWidth) {
                                    newPanX.coerceIn(
                                        screenWidth - scaledW - dragSafeMargin,
                                        dragSafeMargin
                                    )
                                } else {
                                    (screenWidth - scaledW) / 2f
                                }

                                newPanY = if (scaledH >= screenHeight) {
                                    newPanY.coerceIn(
                                        screenHeight - scaledH - dragSafeMargin,
                                        dragSafeMargin
                                    )
                                } else {
                                    (screenHeight - scaledH) / 2f
                                }

                                panX = newPanX
                                panY = newPanY
                                zoom = newZoom
                                viewModel.setViewport(zoom, panX, panY)
                            }
                        }
                        .pointerInput(state.showRuler) {
                            if (state.showRuler) {
                                detectTapGestures { offset ->
                                    val cx = (offset.x - panX) / zoom
                                    val cy = (offset.y - panY) / zoom
                                    viewModel.setRulerPoint(cx, cy)
                                }
                            }
                        }
                ) {
                    if (state.isCameraMode) {
                        com.gridraw.app.ui.components.CameraPreview(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!state.hasImage && !state.isCameraMode) {
                        WelcomePlaceholder(
                            onImport = { imageLauncher.launch("image/*") }
                        )
                    } else {
                        GridCanvas(
                            modifier = Modifier.fillMaxSize(),
                            sourceBitmap = state.sourceBitmap,
                            filters = state.filters,
                            grid = state.grid,
                            paperSize = state.paperSize,
                            orientation = state.orientation,
                            customWidthMm = state.customWidthMm,
                            customHeightMm = state.customHeightMm,
                            ppi = state.ppi,
                            zoom = zoom,
                            panX = panX,
                            panY = panY,
                            showRuler = state.showRuler,
                            isCameraMode = state.isCameraMode,
                            rulerP1 = state.rulerPoint1,
                            rulerP2 = state.rulerPoint2,
                            cameraGridOpacity = state.cameraGridOpacity,
                            cameraImageOpacity = state.cameraImageOpacity,
                        )
                    }
                }
            }
        }

        // ── Layer 2: UI Overlays (never haze-blurred)

        // Ruler distance badge
        val dist = viewModel.getMeasuredDistanceMm()
        if (state.showRuler && dist != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Warning.copy(alpha = 0.15f))
                        .border(1.dp, Warning.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "📏 ${String.format("%.1f", dist)} mm",
                        color = Warning,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top-left controls: Back button and Zoom indicator
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onNavigateBack() }
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Rounded.ArrowBack, 
                    contentDescription = "Back to Home", 
                    tint = Color.White.copy(alpha = 0.85f), 
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom indicator
            if (kotlin.math.abs(zoom - 1f) > 0.01f) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${String.format("%.1f", zoom)}×",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Toast
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 80.dp)
        ) {
            ToastHost(
                message = state.toastMessage,
                type = state.toastType,
                onDismiss = { viewModel.dismissToast() }
            )
        }

        // Floating Tool Dock
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            ToolDock(
                hazeState = hazeState,
                visible = !state.isPanelOpen,
                showRuler = state.showRuler,
                showGrid = state.grid.enabled,
                isCameraMode = state.isCameraMode,
                isArProject = state.isArProject,
                onFitScreen = {
                    val ppMm = state.ppi / 25.4f
                    val (rawW, rawH) = when (state.paperSize) {
                        com.gridraw.app.data.models.PaperSize.CUSTOM ->
                            Pair(state.customWidthMm, state.customHeightMm)
                        else -> {
                            val base = Pair(state.paperSize.widthMm, state.paperSize.heightMm)
                            if (state.orientation == com.gridraw.app.data.models.Orientation.LANDSCAPE)
                                Pair(base.second, base.first)
                            else base
                        }
                    }
                    val canvasW = (rawW * ppMm).coerceAtLeast(100f)
                    val canvasH = (rawH * ppMm).coerceAtLeast(100f)

                    val metrics = context.resources.displayMetrics
                    val screenW = metrics.widthPixels.toFloat()
                    val screenH = metrics.heightPixels.toFloat()

                    val fitZoom = minOf(
                        screenW * 0.90f / canvasW,
                        screenH * 0.85f / canvasH
                    ).coerceIn(0.05f, 10f)

                    zoom = fitZoom
                    panX = (screenW - canvasW * fitZoom) / 2f
                    panY = (screenH - canvasH * fitZoom) / 2f
                    viewModel.setViewport(zoom, panX, panY)
                },
                onToggleGrid = {
                    viewModel.updateGrid(state.grid.copy(enabled = !state.grid.enabled))
                },
                onOpenPanel = { viewModel.openPanel() },
                onToggleRuler = { viewModel.toggleRuler() },
                onCameraMode = { viewModel.toggleCameraMode() }
            )
        }

        // Control Panel
        ControlPanel(
            isOpen = state.isPanelOpen,
            activeTab = state.activeTab,
            paperSize = state.paperSize,
            orientation = state.orientation,
            customWidthMm = state.customWidthMm,
            customHeightMm = state.customHeightMm,
            filters = state.filters,
            grid = state.grid,
            ppi = state.ppi,
            hasImage = state.hasImage,
            isArProject = state.isArProject,
            isCameraMode = state.isCameraMode,
            cameraGridOpacity = state.cameraGridOpacity,
            cameraImageOpacity = state.cameraImageOpacity,
            onClose = { viewModel.closePanel() },
            onTabChange = { viewModel.setActiveTab(it) },
            onPaperSizeChange = { viewModel.setPaperSize(it) },
            onOrientationToggle = { viewModel.toggleOrientation() },
            onCustomDimsChange = { w, h -> viewModel.setCustomDimensions(w, h) },
            onFiltersChange = { viewModel.updateFilters(it) },
            onFiltersReset = { viewModel.resetFilters() },
            onGridChange = { viewModel.updateGrid(it) },
            onPpiChange = { viewModel.setPpi(it) },
            onExport = { viewModel.exportImage(context) {} },
            onExtractPalette = { viewModel.extractPalette() },
            onCameraGridOpacityChange = { viewModel.setCameraGridOpacity(it) },
            onCameraImageOpacityChange = { viewModel.setCameraImageOpacity(it) },
            hazeState = hazeState
        )

        // Palette Sheet
        if (state.showPaletteSheet) {
            PaletteBottomSheet(
                colors = state.extractedPalette,
                onDismiss = { viewModel.closePaletteSheet() }
            )
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                    Text(state.loadingMessage, color = TextMuted, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── Welcome Placeholder ────────────────────────────────────────────────────────

@Composable
private fun WelcomePlaceholder(onImport: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(BgPanel.copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(28.dp))
                .padding(horizontal = 44.dp, vertical = 40.dp)
        ) {
            Icon(
                Icons.Rounded.AddPhotoAlternate,
                contentDescription = null,
                tint = TextMain.copy(alpha = 0.65f),
                modifier = Modifier.size(60.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "No Image Loaded",
                color = TextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Import a reference image to\nstart drawing your grid",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 13.dp)
            ) {
                Icon(Icons.Rounded.FolderOpen, null, Modifier.size(18.dp), tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Browse Gallery", fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
        }
    }
}

// ── Palette Sheet ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaletteBottomSheet(colors: List<Long>, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgPanel,
        dragHandle = {
            // Custom drag handle that matches the dark theme
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Extracted Palette",
                color = TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Dominant colors from your reference image",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(24.dp))

            if (colors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No colors extracted yet", color = TextDim, fontSize = 14.sp)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.take(6).forEach { raw ->
                        // FIX: palette Long values from Palette API are ARGB ints stored as Long.
                        // Cast to Int first to preserve sign, then extract channels correctly.
                        val argb = raw.toInt()
                        val r = (argb shr 16) and 0xFF
                        val g = (argb shr 8) and 0xFF
                        val b = argb and 0xFF
                        val hex = "#%02X%02X%02X".format(r, g, b)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(r, g, b))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                hex,
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
package com.gridraw.app.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.ui.components.*
import com.gridraw.app.ui.theme.*
import com.gridraw.app.viewmodel.EditorViewModel


// ──────────────────────────────────────────────────────────────────────────────
// EditorScreen — Main Canvas + Controls
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Viewport gesture state
    var zoom by remember { mutableFloatStateOf(state.viewportZoom.coerceAtLeast(0.05f)) }
    var panX by remember { mutableFloatStateOf(state.viewportOffsetX) }
    var panY by remember { mutableFloatStateOf(state.viewportOffsetY) }

    BackHandler { onNavigateBack() }

    // Image picker for replacing image
    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadImageFromUri(context, it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgRoot)
    ) {
        // ── Viewport (pannable + zoomable canvas) ─────────────────────────────
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = constraints.maxWidth.toFloat()
            val screenHeight = constraints.maxHeight.toFloat()
            
            val ppMm = state.ppi / 25.4f
            val (rawW, rawH) = when (state.paperSize) {
                com.gridraw.app.data.models.PaperSize.CUSTOM -> Pair(state.customWidthMm, state.customHeightMm)
                else -> {
                    val base = Pair(state.paperSize.widthMm, state.paperSize.heightMm)
                    if (state.orientation == com.gridraw.app.data.models.Orientation.LANDSCAPE) Pair(base.second, base.first)
                    else base
                }
            }
            val canvasW = (rawW * ppMm).coerceAtLeast(100f)
            val canvasH = (rawH * ppMm).coerceAtLeast(100f)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(state.paperSize, state.orientation, state.ppi) {
                        detectTransformGestures { centroid, pan, gestureZoom, _ ->
                            val minZoom = maxOf(screenWidth / canvasW, screenHeight / canvasH)
                            val newZoom = (zoom * gestureZoom).coerceIn(minZoom, 10f)
                            val zoomFactor = newZoom / zoom
                            
                            var newPanX = centroid.x - (centroid.x - panX) * zoomFactor + pan.x
                            var newPanY = centroid.y - (centroid.y - panY) * zoomFactor + pan.y
                            
                            val scaledW = canvasW * newZoom
                            val scaledH = canvasH * newZoom
                            
                            // Clamp pan bounds so edges don't leave screen
                            newPanX = newPanX.coerceIn(screenWidth - scaledW, 0f)
                            newPanY = newPanY.coerceIn(screenHeight - scaledH, 0f)
                            
                            panX = newPanX
                            panY = newPanY
                            zoom = newZoom
                            viewModel.setViewport(zoom, panX, panY)
                        }
                    }
                .pointerInput(state.showRuler) {
                    if (state.showRuler) {
                        detectTapGestures { offset ->
                            // Convert screen to canvas coordinates
                            val cx = (offset.x - panX) / zoom
                            val cy = (offset.y - panY) / zoom
                            viewModel.setRulerPoint(cx, cy)
                        }
                    }
                }
        ) {
            // If no image: show welcome prompt inside viewport
            if (!state.hasImage) {
                WelcomePlaceholder(
                    onImport = { imageLauncher.launch("image/*") }
                )
            } else {
                // Grid Canvas
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
                    rulerP1 = state.rulerPoint1,
                    rulerP2 = state.rulerPoint2,
                )
            }
            }
        }
        
        // ── Ruler Distance Overlay ────────────────────────────────────────────
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

        // ── Toast ─────────────────────────────────────────────────────────────
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

        // ── Floating Tool Dock ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            ToolDock(
                visible = !state.isPanelOpen,
                zoomPercent = (zoom * 100).toInt(),
                canUndo = state.canUndo,
                showRuler = state.showRuler,
                onZoomIn = {
                    zoom = (zoom * 1.2f).coerceAtMost(10f)
                    viewModel.setViewport(zoom, panX, panY)
                },
                onZoomOut = {
                    zoom = (zoom * 0.8f).coerceAtLeast(0.05f)
                    viewModel.setViewport(zoom, panX, panY)
                },
                onFitScreen = {
                    // Reset to fit; will center
                    zoom = 0.5f
                    panX = 40f
                    panY = 80f
                    viewModel.setViewport(zoom, panX, panY)
                },
                onUndo = { viewModel.undo() },
                onOpenPanel = { viewModel.openPanel() },
                onToggleRuler = { viewModel.toggleRuler() },
                onCameraMode = { viewModel.toggleCameraMode() }
            )
        }

        // ── Control Panel ─────────────────────────────────────────────────────
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
            onExtractPalette = { viewModel.extractPalette() }
        )

        // ── Palette Sheet ─────────────────────────────────────────────────────
        if (state.showPaletteSheet) {
            PaletteBottomSheet(
                colors = state.extractedPalette,
                onDismiss = { viewModel.closePaletteSheet() }
            )
        }

        // ── Loading Overlay ───────────────────────────────────────────────────
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
                .background(BgPanel.copy(alpha = 0.9f))
                .padding(40.dp)
        ) {
            Icon(
                Icons.Rounded.AddPhotoAlternate,
                contentDescription = null,
                tint = AccentBlue.copy(alpha = 0.7f),
                modifier = Modifier.size(64.dp)
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
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.FolderOpen, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Browse Gallery", fontWeight = FontWeight.SemiBold)
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
        containerColor = BgPanel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Text("Extracted Palette", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Dominant colors from your reference image", color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            if (colors.isEmpty()) {
                Text("No colors extracted", color = TextDim, fontSize = 14.sp)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colors.take(6).forEach { hex ->
                        val r = ((hex shr 16) and 0xFF).toInt()
                        val g = ((hex shr 8) and 0xFF).toInt()
                        val b = (hex and 0xFF).toInt()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(r, g, b))
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "#${Integer.toHexString(r).padStart(2, '0')}${Integer.toHexString(g).padStart(2, '0')}${Integer.toHexString(b).padStart(2, '0')}".uppercase(),
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

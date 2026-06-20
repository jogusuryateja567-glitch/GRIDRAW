package com.gridraw.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.data.models.*
import com.gridraw.app.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.ui.graphics.graphicsLayer

// ──────────────────────────────────────────────────────────────────────────────
// ControlPanel — Professional Settings Bottom Sheet with Glassmorphic Design
// ──────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanel(
    isOpen: Boolean,
    activeTab: Int,
    paperSize: PaperSize,
    orientation: Orientation,
    customWidthMm: Float,
    customHeightMm: Float,
    filters: ImageFilters,
    grid: GridConfig,
    ppi: Float,
    hasImage: Boolean,
    isCameraMode: Boolean,
    isArProject: Boolean,
    cameraGridOpacity: Float,
    cameraImageOpacity: Float,
    onClose: () -> Unit,
    onTabChange: (Int) -> Unit,
    onPaperSizeChange: (PaperSize) -> Unit,
    onOrientationToggle: () -> Unit,
    onCustomDimsChange: (Float, Float) -> Unit,
    onFiltersChange: (ImageFilters) -> Unit,
    onFiltersReset: () -> Unit,
    onGridChange: (GridConfig) -> Unit,
    onPpiChange: (Float) -> Unit,
    onExport: () -> Unit,
    onExtractPalette: () -> Unit,
    onCameraGridOpacityChange: (Float) -> Unit,
    onCameraImageOpacityChange: (Float) -> Unit,
    hazeState: HazeState,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Dynamic tab list: Grid → Canvas → Image → Export (+ Camera only in AR mode)
    val tabs = remember(isCameraMode) {
        buildList {
            add(TabItem(Icons.Rounded.GridOn, "Grid", TabContent.GRID))
            add(TabItem(Icons.Rounded.GridView, "Canvas", TabContent.CANVAS))
            add(TabItem(Icons.Rounded.Tune, "Image", TabContent.IMAGE))
            if (isCameraMode || isArProject) {
                add(TabItem(Icons.Rounded.PhotoCamera, "Camera", TabContent.CAMERA))
            }
            add(TabItem(Icons.Rounded.FileDownload, "Export", TabContent.EXPORT))
        }
    }

    // Clamp activeTab to valid range when tabs change
    val safeActiveTab = activeTab.coerceIn(0, tabs.lastIndex.coerceAtLeast(0))

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            contentColor = TextMain,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        style = HazeStyle(
                            blurRadius = 30.dp,
                            tint = Color(0x1A191924)
                        )
                    )
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            ) {
                // Drag handle for visual feedback
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    )
                }

                // Professional tab row with indicator
                PanelTabRow(
                    tabs = tabs,
                    activeTab = safeActiveTab,
                    onTabChange = onTabChange
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                // Tab content with smooth scrolling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    val currentContent = tabs.getOrNull(safeActiveTab)?.content ?: TabContent.GRID
                    when (currentContent) {
                        TabContent.GRID -> GridTab(
                            grid = grid,
                            onGridChange = onGridChange
                        )
                        TabContent.CANVAS -> CanvasTab(
                            paperSize = paperSize,
                            orientation = orientation,
                            customWidthMm = customWidthMm,
                            customHeightMm = customHeightMm,
                            ppi = ppi,
                            onPaperSizeChange = onPaperSizeChange,
                            onOrientationToggle = onOrientationToggle,
                            onCustomDimsChange = onCustomDimsChange,
                            onPpiChange = onPpiChange
                        )
                        TabContent.IMAGE -> ImageTab(
                            filters = filters,
                            hasImage = hasImage,
                            onFiltersChange = onFiltersChange,
                            onFiltersReset = onFiltersReset,
                            onExtractPalette = onExtractPalette
                        )
                        TabContent.CAMERA -> CameraTab(
                            cameraGridOpacity = cameraGridOpacity,
                            cameraImageOpacity = cameraImageOpacity,
                            onCameraGridOpacityChange = onCameraGridOpacityChange,
                            onCameraImageOpacityChange = onCameraImageOpacityChange
                        )
                        TabContent.EXPORT -> ExportTab(
                            hasImage = hasImage,
                            onExport = onExport
                        )
                    }
                }
            }
        }
    }
}

// ── Tab Content Enum ──────────────────────────────────────────────────────────

private enum class TabContent {
    GRID, CANVAS, IMAGE, CAMERA, EXPORT
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

private data class TabItem(val icon: ImageVector, val label: String, val content: TabContent)

@Composable
private fun PanelTabRow(tabs: List<TabItem>, activeTab: Int, onTabChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == activeTab

            TabButton(
                selected = selected,
                icon = tab.icon,
                label = tab.label,
                onClick = { onTabChange(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) Color.White.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = 1.5.dp,
                color = if (selected) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .semantics {
                contentDescription = if (selected) "$label tab selected" else label
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) Color.White else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )

        // Animated underline indicator
        if (selected) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

// ── Grid Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun GridTab(
    grid: GridConfig,
    onGridChange: (GridConfig) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader("Appearance")

        ToggleRow(
            label = "Show Grid",
            icon = Icons.Rounded.GridOn,
            enabled = grid.enabled,
            onToggle = { onGridChange(grid.copy(enabled = !grid.enabled)) }
        )

        if (grid.enabled) {
            StyledSlider(
                label = "Grid Size",
                value = grid.sizeMm,
                range = 5f..100f,
                displayValue = "${grid.sizeMm.toInt()}mm",
                onValueChange = { onGridChange(grid.copy(sizeMm = it)) }
            )

            StyledSlider(
                label = "Opacity",
                value = grid.opacityPct.toFloat(),
                range = 0f..100f,
                displayValue = "${grid.opacityPct}%",
                onValueChange = { onGridChange(grid.copy(opacityPct = it.toInt())) }
            )

            StyledSlider(
                label = "Line Thickness",
                value = grid.thickness,
                range = 0.5f..4f,
                displayValue = "%.1f".format(grid.thickness),
                onValueChange = { onGridChange(grid.copy(thickness = it)) }
            )

            SectionHeader("Composition Guides", modifier = Modifier.paddingTop(8.dp))

            ToggleRow(
                label = "Diagonals",
                icon = Icons.Rounded.Edit,
                enabled = grid.showDiagonals,
                onToggle = { onGridChange(grid.copy(showDiagonals = !grid.showDiagonals)) }
            )

            ToggleRow(
                label = "Rule of Thirds",
                icon = Icons.Rounded.CropSquare,
                enabled = grid.showThirds,
                onToggle = { onGridChange(grid.copy(showThirds = !grid.showThirds)) }
            )

            ToggleRow(
                label = "Horizontal Symmetry",
                icon = Icons.Rounded.HorizontalRule,
                enabled = grid.showSymmetryH,
                onToggle = { onGridChange(grid.copy(showSymmetryH = !grid.showSymmetryH)) }
            )

            ToggleRow(
                label = "Vertical Symmetry",
                icon = Icons.Rounded.VerticalAlignCenter,
                enabled = grid.showSymmetryV,
                onToggle = { onGridChange(grid.copy(showSymmetryV = !grid.showSymmetryV)) }
            )

            ToggleRow(
                label = "Radial Symmetry",
                icon = Icons.Rounded.Lens,
                enabled = grid.showSymmetryRadial,
                onToggle = { onGridChange(grid.copy(showSymmetryRadial = !grid.showSymmetryRadial)) }
            )

            if (grid.showSymmetryRadial) {
                StyledSlider(
                    label = "Radial Segments",
                    value = grid.symmetrySegments.toFloat(),
                    range = 2f..36f,
                    displayValue = "${grid.symmetrySegments}",
                    onValueChange = { onGridChange(grid.copy(symmetrySegments = it.toInt())) }
                )
            }

            ToggleRow(
                label = "Show Labels",
                icon = Icons.Rounded.Label,
                enabled = grid.showLabels,
                onToggle = { onGridChange(grid.copy(showLabels = !grid.showLabels)) }
            )
        }
    }
}

// ── Canvas Tab ────────────────────────────────────────────────────────────────

@Composable
private fun CanvasTab(
    paperSize: PaperSize,
    orientation: Orientation,
    customWidthMm: Float,
    customHeightMm: Float,
    ppi: Float,
    onPaperSizeChange: (PaperSize) -> Unit,
    onOrientationToggle: () -> Unit,
    onCustomDimsChange: (Float, Float) -> Unit,
    onPpiChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionHeader("Paper Size")

        // Paper size selector
        PaperSizeGrid(
            selectedSize = paperSize,
            onSizeSelected = onPaperSizeChange
        )

        ToggleRow(
            label = "Landscape Orientation",
            icon = if (orientation == Orientation.LANDSCAPE) Icons.Rounded.ScreenRotation else Icons.Rounded.ScreenLockRotation,
            enabled = orientation == Orientation.LANDSCAPE,
            onToggle = onOrientationToggle
        )

        if (paperSize == PaperSize.CUSTOM) {
            SectionHeader("Custom Dimensions", modifier = Modifier.paddingTop(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumericInput(
                    label = "Width",
                    value = customWidthMm,
                    suffix = "mm",
                    onValueChange = { onCustomDimsChange(it, customHeightMm) },
                    modifier = Modifier.weight(1f)
                )
                NumericInput(
                    label = "Height",
                    value = customHeightMm,
                    suffix = "mm",
                    onValueChange = { onCustomDimsChange(customWidthMm, it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionHeader("Calibration", modifier = Modifier.paddingTop(8.dp))

        StyledSlider(
            label = "PPI (Print Resolution)",
            value = ppi,
            range = 50f..600f,
            displayValue = "%.0f PPI".format(ppi),
            onValueChange = onPpiChange
        )

        InfoBox(
            "Tip: Set PPI to match your device's screen density for accurate grid sizing. " +
            "Standard values: 72 (web), 150 (mobile), 300 (print)."
        )
    }
}

// ── Image Tab ─────────────────────────────────────────────────────────────────

@Composable
private fun ImageTab(
    filters: ImageFilters,
    hasImage: Boolean,
    onFiltersChange: (ImageFilters) -> Unit,
    onFiltersReset: () -> Unit,
    onExtractPalette: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (!hasImage) {
            EmptyStateCard(
                icon = Icons.Rounded.ImageNotSupported,
                title = "No Image Loaded",
                message = "Import an image from your gallery to adjust filters and effects."
            )
            return@Column
        }

        SectionHeader("Adjustments")

        StyledSlider(
            label = "Brightness",
            value = filters.brightness.toFloat(),
            range = -100f..100f,
            displayValue = (if (filters.brightness > 0) "+${filters.brightness}" else "${filters.brightness}") + "%",
            onValueChange = { onFiltersChange(filters.copy(brightness = it.toInt())) }
        )

        StyledSlider(
            label = "Contrast",
            value = filters.contrast.toFloat(),
            range = -100f..100f,
            displayValue = (if (filters.contrast > 0) "+${filters.contrast}" else "${filters.contrast}") + "%",
            onValueChange = { onFiltersChange(filters.copy(contrast = it.toInt())) }
        )

        StyledSlider(
            label = "Saturation",
            value = filters.saturation.toFloat(),
            range = 0f..200f,
            displayValue = "${filters.saturation}%",
            onValueChange = { onFiltersChange(filters.copy(saturation = it.toInt())) }
        )

        ToggleRow(
            label = "Grayscale",
            icon = Icons.Rounded.Tonality,
            enabled = filters.grayscale,
            onToggle = { onFiltersChange(filters.copy(grayscale = !filters.grayscale)) }
        )

        Spacer(Modifier.height(8.dp))

        PrimaryButton(
            label = "Reset Filters",
            icon = Icons.Rounded.RestartAlt,
            onClick = onFiltersReset
        )

        PrimaryButton(
            label = "Extract Color Palette",
            icon = Icons.Rounded.Palette,
            onClick = onExtractPalette
        )
    }
}

// ── Camera Tab ────────────────────────────────────────────────────────────────

@Composable
private fun CameraTab(
    cameraGridOpacity: Float,
    cameraImageOpacity: Float,
    onCameraGridOpacityChange: (Float) -> Unit,
    onCameraImageOpacityChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        InfoBox(
            "These settings apply only in AR Camera mode. Regular grid settings remain separate."
        )

        SectionHeader("Overlay Opacity")

        StyledSlider(
            label = "Grid Opacity",
            value = cameraGridOpacity * 100f,
            range = 0f..100f,
            displayValue = "${(cameraGridOpacity * 100).toInt()}%",
            onValueChange = { onCameraGridOpacityChange(it / 100f) }
        )

        StyledSlider(
            label = "Image Overlay Opacity",
            value = cameraImageOpacity * 100f,
            range = 0f..100f,
            displayValue = "${(cameraImageOpacity * 100).toInt()}%",
            onValueChange = { onCameraImageOpacityChange(it / 100f) }
        )

        InfoBox(
            "Set Image Overlay to 0% for a clean camera view, or 100% for full reference visibility."
        )
    }
}

// ── Export Tab ────────────────────────────────────────────────────────────────

@Composable
private fun ExportTab(
    hasImage: Boolean,
    onExport: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FileDownload,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            "Export Canvas with Grid",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            "Save your reference image with the grid overlay baked in at full resolution.",
            color = TextMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth()
        )

        InfoBox(
            listOf(
                "Format" to "JPEG, 95% quality",
                "Resolution" to "Full paper size at calibrated PPI",
                "Save Location" to "Pictures/GRIDRAW"
            )
        )

        Spacer(Modifier.height(8.dp))

        PrimaryButton(
            label = "Save to Gallery",
            icon = Icons.Rounded.FileDownload,
            onClick = onExport,
            enabled = hasImage,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Helper Composables ────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = modifier.padding(top = 4.dp)
    )
}

@Composable
fun ToggleRow(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                1.5.dp,
                if (enabled) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                color = if (enabled) Color.White else TextMuted,
                fontSize = 14.sp,
                fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.9f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = TextMuted.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun StyledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(displayValue, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}

@Composable
fun InfoBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            message,
            color = TextDim,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun InfoBox(items: List<Pair<String, String>>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(key, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                    Text(value, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(32.dp), tint = TextDim)
        }

        Spacer(Modifier.height(16.dp))

        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun PrimaryButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(if (enabled) 1f else 0.3f),
            disabledContainerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = if (enabled) Color.Black else TextDim)
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (enabled) Color.Black else TextDim
        )
    }
}

@Composable
fun NumericInput(
    label: String,
    value: Float,
    suffix: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = String.format("%.1f", value),
            onValueChange = { s ->
                s.toFloatOrNull()?.let { onValueChange(it) }
            },
            suffix = { Text(suffix, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = gridTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun PaperSizeGrid(
    selectedSize: PaperSize,
    onSizeSelected: (PaperSize) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            Triple(PaperSize.LETTER, "Letter", "8.5\" × 11\""),
            Triple(PaperSize.A4, "A4", "210 × 297mm"),
            Triple(PaperSize.A3, "A3", "297 × 420mm"),
            Triple(PaperSize.CUSTOM, "Custom", "User defined")
        ).forEach { (size, name, dims) ->
            PaperSizeOption(
                selected = selectedSize == size,
                name = name,
                dimensions = dims,
                onClick = { onSizeSelected(size) }
            )
        }
    }
}

@Composable
fun PaperSizeOption(
    selected: Boolean,
    name: String,
    dimensions: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                1.5.dp,
                if (selected) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(name, color = if (selected) Color.White else TextMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(2.dp))
            Text(dimensions, color = TextMuted, fontSize = 12.sp)
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) Color.White else Color.White.copy(alpha = 0.15f))
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Rounded.Check, null, Modifier.size(12.dp), tint = Color.Black)
            }
        }
    }
}

@Composable
fun gridTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White.copy(alpha = 0.5f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    focusedLabelColor = Color.White.copy(alpha = 0.8f),
    unfocusedLabelColor = TextMuted,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = TextMain,
    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
    focusedContainerColor = Color.White.copy(alpha = 0.1f)
)

private fun Modifier.paddingTop(dp: androidx.compose.ui.unit.Dp) = this.padding(top = dp)

private fun Modifier.scale(scale: Float) = this.graphicsLayer { scaleX = scale; scaleY = scale }
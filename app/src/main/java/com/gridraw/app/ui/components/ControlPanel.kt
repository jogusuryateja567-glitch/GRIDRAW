package com.gridraw.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridraw.app.data.models.*
import com.gridraw.app.ui.theme.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.HazeStyle

// ─────────────────────────────────────────────────────────────────────────────
// Control Panel — Bottom Sheet with Tabs
// ─────────────────────────────────────────────────────────────────────────────

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
    hazeState: HazeState,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isOpen) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            contentColor = TextMain,
            // FIX: dragHandle null so we draw our own inside the glass container
            dragHandle = null,
            // Ensure sheet has a real min height
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .hazeChild(
                        state = hazeState,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        style = HazeStyle(blurRadius = 30.dp, tint = BgCard)
                    )
                    .border(
                        1.dp,
                        BorderGlass,
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
            ) {
                // Drag handle
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .width(40.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }

                // Tab Row
                PanelTabRow(activeTab = activeTab, onTabChange = onTabChange)

                HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

                // Tab Content with AnimatedContent for smooth tab transitions
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally { dir * it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { -dir * it / 4 } + fadeOut())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 500.dp),
                    label = "tab_content"
                ) { tab ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        when (tab) {
                            0 -> CanvasTab(
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
                            1 -> ImageTab(
                                filters = filters,
                                hasImage = hasImage,
                                onFiltersChange = onFiltersChange,
                                onFiltersReset = onFiltersReset,
                                onExtractPalette = onExtractPalette
                            )
                            2 -> GridTab(grid = grid, onGridChange = onGridChange)
                            3 -> ExportTab(hasImage = hasImage, onExport = onExport)
                        }
                    }
                }
            }
        }
    }
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

private data class TabItem(val icon: ImageVector, val label: String)

private val TABS = listOf(
    TabItem(Icons.Rounded.GridView, "Canvas"),
    TabItem(Icons.Rounded.Tune, "Image"),
    TabItem(Icons.Rounded.GridOn, "Grid"),
    TabItem(Icons.Rounded.FileDownload, "Export"),
)

@Composable
private fun PanelTabRow(activeTab: Int, onTabChange: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TABS.forEachIndexed { index, tab ->
            val selected = index == activeTab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTabChange(index)
                    }
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) Color.White else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tab.label,
                    color = if (selected) Color.White else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                Spacer(Modifier.height(4.dp))
                // Animated indicator dot
                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    Box(
                        Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                if (!selected) Spacer(Modifier.height(2.dp))
            }
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
    var ppiText by remember(ppi) { mutableStateOf(ppi.toInt().toString()) }
    var customW by remember(customWidthMm) { mutableStateOf(customWidthMm.toInt().toString()) }
    var customH by remember(customHeightMm) { mutableStateOf(customHeightMm.toInt().toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader("Paper Size")

        // FIX: LazyRow prevents overflow on small screens
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sizes = PaperSize.entries.filter { it != PaperSize.CUSTOM }
            items(sizes) { size ->
                val selected = paperSize == size
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color.White.copy(alpha = 0.15f) else BgInput)
                        .border(1.dp, if (selected) Color.White else BorderLight, RoundedCornerShape(10.dp))
                        .clickable { onPaperSizeChange(size) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = size.name,
                        color = if (selected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        // Custom dimensions button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (paperSize == PaperSize.CUSTOM) Color.White.copy(alpha = 0.15f) else BgInput)
                .border(1.dp, if (paperSize == PaperSize.CUSTOM) Color.White else BorderLight, RoundedCornerShape(10.dp))
                .clickable { onPaperSizeChange(PaperSize.CUSTOM) }
                .padding(14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "Custom Dimensions",
                color = if (paperSize == PaperSize.CUSTOM) Color.White else TextMuted,
                fontSize = 13.sp
            )
        }

        AnimatedVisibility(
            visible = paperSize == PaperSize.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customW,
                    onValueChange = {
                        customW = it
                        it.toFloatOrNull()?.let { v -> onCustomDimsChange(v, customH.toFloatOrNull() ?: 200f) }
                    },
                    label = { Text("Width (mm)") },
                    modifier = Modifier.weight(1f),
                    colors = gridTextFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = customH,
                    onValueChange = {
                        customH = it
                        it.toFloatOrNull()?.let { v -> onCustomDimsChange(customW.toFloatOrNull() ?: 200f, v) }
                    },
                    label = { Text("Height (mm)") },
                    modifier = Modifier.weight(1f),
                    colors = gridTextFieldColors(),
                    singleLine = true
                )
            }
        }

        SectionHeader("Orientation")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Orientation.PORTRAIT, Orientation.LANDSCAPE).forEach { ori ->
                val sel = orientation == ori
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) Color.White.copy(alpha = 0.15f) else BgInput)
                        .border(1.dp, if (sel) Color.White else BorderLight, RoundedCornerShape(10.dp))
                        .clickable { if (orientation != ori) onOrientationToggle() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (ori == Orientation.PORTRAIT)
                            Icons.Rounded.StayCurrentPortrait else Icons.Rounded.StayCurrentLandscape,
                        contentDescription = ori.name,
                        tint = if (sel) Color.White else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        ori.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (sel) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (sel) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        SectionHeader("Screen PPI (Calibration)")
        OutlinedTextField(
            value = ppiText,
            onValueChange = {
                ppiText = it
                it.toFloatOrNull()?.let { v -> onPpiChange(v) }
            },
            label = { Text("Pixels per inch") },
            modifier = Modifier.fillMaxWidth(),
            colors = gridTextFieldColors(),
            singleLine = true,
            trailingIcon = {
                TextButton(onClick = { onPpiChange(96f); ppiText = "96" }) {
                    Text("Reset", color = TextMuted, fontSize = 12.sp)
                }
            }
        )
        Text(
            "Hold a physical ruler against the screen to calibrate.",
            color = TextDim,
            fontSize = 11.sp,
            lineHeight = 16.sp
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader("Adjustments")
            TextButton(onClick = onFiltersReset) {
                Text("Reset All", color = TextMuted, fontSize = 12.sp)
            }
        }

        ToggleRow(
            label = "Black & White",
            icon = Icons.Rounded.Lens,
            enabled = filters.grayscale,
            onToggle = { onFiltersChange(filters.copy(grayscale = !filters.grayscale)) }
        )

        StyledSlider(
            label = "Brightness",
            value = filters.brightness,
            range = 0f..200f,
            displayValue = "${filters.brightness.toInt()}%",
            onValueChange = { onFiltersChange(filters.copy(brightness = it)) }
        )
        StyledSlider(
            label = "Contrast",
            value = filters.contrast,
            range = 0f..200f,
            displayValue = "${filters.contrast.toInt()}%",
            onValueChange = { onFiltersChange(filters.copy(contrast = it)) }
        )
        StyledSlider(
            label = "Saturation",
            value = filters.saturation,
            range = 0f..200f,
            displayValue = "${filters.saturation.toInt()}%",
            onValueChange = { onFiltersChange(filters.copy(saturation = it)) }
        )
        StyledSlider(
            label = "Blur (Simplify)",
            value = filters.blur,
            range = 0f..10f,
            displayValue = "${"%.1f".format(filters.blur)}px",
            onValueChange = { onFiltersChange(filters.copy(blur = it)) }
        )

        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)

        Button(
            onClick = onExtractPalette,
            enabled = hasImage,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = TextDim
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Extract Color Palette", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Grid Tab ──────────────────────────────────────────────────────────────────

@Composable
private fun GridTab(
    grid: GridConfig,
    onGridChange: (GridConfig) -> Unit,
) {
    var sizeText by remember(grid.sizeMm) { mutableStateOf(grid.sizeMm.toString()) }
    var thickText by remember(grid.thickness) { mutableStateOf(grid.thickness.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader("Grid Lines")

        ToggleRow(
            label = "Show Grid",
            icon = Icons.Rounded.GridOn,
            enabled = grid.enabled,
            onToggle = { onGridChange(grid.copy(enabled = !grid.enabled)) }
        )

        AnimatedVisibility(
            visible = grid.enabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sizeText,
                        onValueChange = {
                            sizeText = it
                            it.toFloatOrNull()?.let { v -> onGridChange(grid.copy(sizeMm = v)) }
                        },
                        label = { Text("Cell Size (mm)") },
                        modifier = Modifier.weight(1f),
                        colors = gridTextFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = thickText,
                        onValueChange = {
                            thickText = it
                            it.toFloatOrNull()?.let { v -> onGridChange(grid.copy(thickness = v)) }
                        },
                        label = { Text("Line Width") },
                        modifier = Modifier.weight(1f),
                        colors = gridTextFieldColors(),
                        singleLine = true
                    )
                }

                StyledSlider(
                    label = "Opacity",
                    value = grid.opacityPct.toFloat(),
                    range = 10f..100f,
                    displayValue = "${grid.opacityPct}%",
                    onValueChange = { onGridChange(grid.copy(opacityPct = it.toInt())) }
                )

                SectionHeader("Grid Color")
                // FIX: 44.dp tap targets for all color swatches
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        0xFF5B8DFF, 0xFFFF4D6A, 0xFF00E676,
                        0xFFFFBE00, 0xFFFFFFFF, 0xFF000000
                    ).forEach { hex ->
                        val isSelected = grid.colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(
                                        red = ((hex shr 16) and 0xFF).toInt(),
                                        green = ((hex shr 8) and 0xFF).toInt(),
                                        blue = (hex and 0xFF).toInt()
                                    )
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable { onGridChange(grid.copy(colorHex = hex)) }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
        SectionHeader("Overlays")

        ToggleRow("Grid Labels (A1, B2…)", Icons.Rounded.Label, grid.showLabels) {
            onGridChange(grid.copy(showLabels = !grid.showLabels))
        }
        ToggleRow("Diagonals (X-Grid)", Icons.Rounded.Close, grid.showDiagonals) {
            onGridChange(grid.copy(showDiagonals = !grid.showDiagonals))
        }
        ToggleRow("Rule of Thirds", Icons.Rounded.ViewQuilt, grid.showThirds) {
            onGridChange(grid.copy(showThirds = !grid.showThirds))
        }
        ToggleRow("Symmetry — Horizontal", Icons.Rounded.SwapVert, grid.showSymmetryH) {
            onGridChange(grid.copy(showSymmetryH = !grid.showSymmetryH))
        }
        ToggleRow("Symmetry — Vertical", Icons.Rounded.SwapHoriz, grid.showSymmetryV) {
            onGridChange(grid.copy(showSymmetryV = !grid.showSymmetryV))
        }
        ToggleRow("Radial Symmetry", Icons.Rounded.BlurCircular, grid.showSymmetryRadial) {
            onGridChange(grid.copy(showSymmetryRadial = !grid.showSymmetryRadial))
        }

        AnimatedVisibility(
            visible = grid.showSymmetryRadial,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            StyledSlider(
                label = "Segments",
                value = grid.symmetrySegments.toFloat(),
                range = 2f..36f,
                displayValue = "${grid.symmetrySegments}",
                onValueChange = { onGridChange(grid.copy(symmetrySegments = it.toInt())) }
            )
        }
    }
}

// ── Export Tab ────────────────────────────────────────────────────────────────

@Composable
private fun ExportTab(hasImage: Boolean, onExport: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Icon(
            imageVector = Icons.Rounded.FileDownload,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(44.dp)
        )

        Text(
            "Export a high-resolution image with your grid overlay applied.",
            color = TextMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BgInput)
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoRow("Format", "JPEG · 95% quality")
                InfoRow("Resolution", "Full paper size at set PPI")
                InfoRow("Save Location", "Pictures/GRIDRAW")
            }
        }

        if (!hasImage) {
            Text(
                "Import an image first to enable export.",
                color = TextDim,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onExport,
            enabled = hasImage,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.15f),
                disabledContentColor = TextDim
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Rounded.FileDownload, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Save to Gallery", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

// ── Shared Helper Composables ─────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = modifier
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
    // FIX: Switch onCheckedChange was duplicating the click — removed, Row handles it
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Color.White.copy(alpha = 0.1f) else BgInput)
            .border(
                1.dp,
                if (enabled) Color.White.copy(alpha = 0.4f) else BorderLight,
                RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label,
                color = if (enabled) TextMain else TextMuted,
                fontSize = 14.sp,
                fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
            )
        }
        // FIX: Switch is read-only display — interaction handled by the Row click
        Switch(
            checked = enabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF32D74B),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BgInputHover
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
    val haptic = LocalHapticFeedback.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = TextMuted, fontSize = 13.sp)
            Text(
                displayValue,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(6.dp))
        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = BgInputHover,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

@Composable
fun InfoRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, color = TextMuted, fontSize = 13.sp)
        Text(value, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun gridTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White.copy(alpha = 0.6f),
    unfocusedBorderColor = BorderLight,
    focusedLabelColor = Color.White.copy(alpha = 0.6f),
    unfocusedLabelColor = TextMuted,
    cursorColor = Color.White,
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    unfocusedContainerColor = BgInput,
    focusedContainerColor = BgInputHover
)
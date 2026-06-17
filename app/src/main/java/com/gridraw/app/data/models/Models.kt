package com.gridraw.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

// ──────────────────────────────────────────────────────────────────────────────
// Grid Configuration  (serialized to JSON for Room storage)
// ──────────────────────────────────────────────────────────────────────────────

enum class PaperSize(val widthMm: Float, val heightMm: Float, val label: String) {
    A5(148f, 210f, "A5 (148×210mm)"),
    A4(210f, 297f, "A4 (210×297mm)"),
    A3(297f, 420f, "A3 (297×420mm)"),
    LETTER(215.9f, 279.4f, "Letter (8.5×11in)"),
    CUSTOM(0f, 0f, "Custom")
}

enum class Orientation { PORTRAIT, LANDSCAPE }

enum class GridOverlay { NONE, LABELS, DIAGONALS, THIRDS, SYMMETRY_H, SYMMETRY_V, SYMMETRY_RADIAL }

data class GridConfig(
    val enabled: Boolean = true,
    val sizeMm: Float = 20f,          // Grid cell size in mm
    val thickness: Float = 1f,
    val colorHex: Long = 0xFF5B8DFF,  // Store as Long for serialization
    val opacityPct: Int = 100,
    val showLabels: Boolean = true,
    val showDiagonals: Boolean = false,
    val showThirds: Boolean = false,
    val showSymmetryH: Boolean = false,
    val showSymmetryV: Boolean = false,
    val showSymmetryRadial: Boolean = false,
    val symmetrySegments: Int = 8
) {
    val color: Long get() = colorHex
}

data class ImageFilters(
    val brightness: Float = 100f,   // 0–200
    val contrast: Float = 100f,     // 0–200
    val blur: Float = 0f,           // 0–10
    val saturation: Float = 100f,   // 0–200
    val grayscale: Boolean = false
)

data class CropState(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f
)

// ──────────────────────────────────────────────────────────────────────────────
// Room Entity
// ──────────────────────────────────────────────────────────────────────────────

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "Untitled Project",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null,      // Saved thumbnail file path
    val imageUri: String? = null,           // Source image URI
    val paperSize: String = PaperSize.A4.name,
    val orientation: String = Orientation.PORTRAIT.name,
    val customWidthMm: Float = 200f,
    val customHeightMm: Float = 200f,
    // Serialized JSON fields
    val gridConfigJson: String = "{}",
    val filtersJson: String = "{}",
    val cropJson: String = "{}",
    val ppi: Float = 96f
)

package com.gridraw.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Deep Background ──────────────────────────────────────────────────────────
val BgRoot       = Color(0xFF000000) // Pure black for OLED / spatial feel
val BgPanel      = Color(0xFF141415) // Very dark gray for elevated panels
val BgCard       = Color(0x801A1A1E) // Semi-transparent card
val BgInput      = Color(0x1AFFFFFF) // 10% White for inputs
val BgInputHover = Color(0x33FFFFFF) // 20% White for hover/pressed

// ── Accent Colors ─────────────────────────────────────────────────────────────
val AccentBlue      = Color(0xFFFFFFFF) // Replaced neon blue with Pure White for monochromatic premium feel
val AccentBlueDim   = Color(0x33FFFFFF) // Dim white
val AccentPurple    = Color(0xFFAAAAAA) // Replaced neon purple with subtle silver
val AccentCyan      = Color(0xFFE5E5EA) // Replaced neon cyan with Apple-style off-white
val AccentGradStart = Color(0xFFFFFFFF)
val AccentGradEnd   = Color(0xFFAAAAAA)

// ── Text ──────────────────────────────────────────────────────────────────────
val TextMain  = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF8E8E93) // Apple system gray 1
val TextDim   = Color(0xFF636366) // Apple system gray 2

// ── Borders ───────────────────────────────────────────────────────────────────
val BorderLight = Color(0x1AFFFFFF)  // 10% white
val BorderHover = Color(0x33FFFFFF)  // 20% white
val BorderGlass = Color(0x4DFFFFFF)  // 30% white for glass edges

// ── Status ────────────────────────────────────────────────────────────────────
val Danger  = Color(0xFFFF453A) // Apple red
val Success = Color(0xFF32D74B) // Apple green
val Warning = Color(0xFFFF9F0A) // Apple orange
val Info    = Color(0xFF0A84FF) // Apple blue (kept only for semantic info)

// ── Surface Variants ──────────────────────────────────────────────────────────
val SurfaceVariant   = Color(0x1AFFFFFF) // Frosted overlay base
val OnSurfaceVariant = Color(0xFFEBEBF5)

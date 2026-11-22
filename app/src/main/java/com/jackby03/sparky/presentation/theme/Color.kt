package com.jackby03.sparky.presentation.theme

import androidx.compose.ui.graphics.Color

// ── Dark canvas palette ───────────────────────────────────────────────────────
val BackgroundDark      = Color(0xFF0A0A0A)
val SurfaceDark         = Color(0xFF111111)
val SurfaceVariantDark  = Color(0xFF1A1A1A)
val GridLineDark        = Color(0xFF1E1E1E)

// ── Neon accents (dark theme) ─────────────────────────────────────────────────
val NeonGreen           = Color(0xFF00FF88)
val NeonCyan            = Color(0xFF00E5FF)
val NeonOrange          = Color(0xFFFF6B35)
val NeonRed             = Color(0xFFFF3B5C)
val NeonPurple          = Color(0xFFBB86FC)

// ── Wire / component neon ─────────────────────────────────────────────────────
val WireColor           = Color(0xFF00E5FF)    // Cyan neon wire
val WireActiveColor     = Color(0xFF00FF88)    // Green when selected
val ComponentColor      = Color(0xFFFFFFFF)    // White component symbols
val VoltageNodeColor    = NeonOrange
val GroundColor         = Color(0xFF888888)

// ── Light theme ───────────────────────────────────────────────────────────────
val BackgroundLight     = Color(0xFFF5F5F5)
val SurfaceLight        = Color(0xFFFFFFFF)
val PrimaryLight        = Color(0xFF00897B)    // Teal
val SecondaryLight      = Color(0xFFFF6B35)

// ── On-colors ─────────────────────────────────────────────────────────────────
val OnBackground        = Color(0xFFEEEEEE)
val OnSurface           = Color(0xFFCCCCCC)
val OnPrimary           = Color(0xFF000000)
val OnError             = Color(0xFF000000)

// ── High contrast theme ───────────────────────────────────────────────────────
val HCBackground        = Color(0xFF000000)   // Pure black background
val HCSurface           = Color(0xFF111111)   // Near-black surface
val HCSurfaceVariant    = Color(0xFF1C1C1C)
val HCPrimary           = Color(0xFFFFFF00)   // Bright yellow — maximum contrast on black
val HCSecondary         = Color(0xFF00FFFF)   // Cyan — secondary accent
val HCOnBackground      = Color(0xFFFFFFFF)   // Pure white text
val HCOnSurface         = Color(0xFFFFFFFF)
val HCOnPrimary         = Color(0xFF000000)
val HCError             = Color(0xFFFF4444)
val HCWire              = Color(0xFF00FFFF)   // Bright cyan wires
val HCWireActive        = Color(0xFFFFFF00)   // Yellow when selected
val HCComponent         = Color(0xFFFFFFFF)   // White component symbols
val HCGrid              = Color(0xFF2A2A2A)   // Slightly lighter grid on pure black

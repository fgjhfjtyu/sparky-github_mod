package com.jackby03.sparky.domain.model.settings

/** Controls which color scheme is applied to the app. */
enum class ThemeMode {
    /** Follow the device system setting (default). */
    SYSTEM,
    /** Always use the dark neon theme. */
    DARK,
    /** Always use the light theme. */
    LIGHT,
    /** High contrast: pure black background, bright yellow primary, white text. */
    HIGH_CONTRAST
}

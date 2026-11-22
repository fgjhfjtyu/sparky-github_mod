package com.jackby03.sparky.core.symbols

/**
 * Lookup of schematic glyphs for catalog components (everything that is not one of the
 * seven simulated base elements, which keep their dedicated drawers).
 */
object SymbolLibrary {

    private const val HALF = 24f

    private val glyphs: Map<String, Glyph> by lazy {
        val map = HashMap<String, Glyph>()
        for (chunk in SymbolGlyphData.chunks) {
            for (line in chunk.lines()) {
                val sep = line.indexOf('|')
                if (sep <= 0) continue
                map[line.substring(0, sep)] = GlyphParser.parse(line.substring(sep + 1))
            }
        }
        map
    }

    /** True if an accurate symbol exists for [componentType] (the enum constant name). */
    fun has(componentType: String): Boolean = glyphs.containsKey(componentType)

    fun names(): Set<String> = glyphs.keys

    /**
     * Returns the symbol for [componentType]. Unknown types get a labelled box with
     * [fallbackLabel], so future catalog entries still render before a glyph is drawn for them.
     */
    fun glyphFor(componentType: String, fallbackLabel: String): Glyph =
        glyphs[componentType] ?: fallbackBox(fallbackLabel)

    private fun fallbackBox(label: String): Glyph = Glyph(
        listOf(
            Prim.Line(-HALF, 0f, -20f, 0f),
            Prim.Line(20f, 0f, HALF, 0f),
            Prim.Rect(-20f, -14f, 20f, 14f),
            Prim.Label(0f, 3.2f, if (label.length < 5) 9f else 7f, label.take(7))
        )
    )
}

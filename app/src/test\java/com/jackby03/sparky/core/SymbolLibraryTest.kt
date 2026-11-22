package com.jackby03.sparky.core

import com.jackby03.sparky.core.symbols.GlyphParser
import com.jackby03.sparky.core.symbols.Prim
import com.jackby03.sparky.core.symbols.SymbolLibrary
import com.jackby03.sparky.domain.model.component.ComponentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SymbolLibraryTest {

    /** Types that keep their dedicated drawer in ComponentRenderer instead of a glyph. */
    private val baseTypes = setOf(
        ComponentType.RESISTOR, ComponentType.CAPACITOR, ComponentType.INDUCTOR,
        ComponentType.VOLTAGE_SOURCE, ComponentType.CURRENT_SOURCE,
        ComponentType.DIODE, ComponentType.GROUND
    )

    private val catalogTypes get() = ComponentType.values().filter { it !in baseTypes }

    @Test
    fun everyCatalogTypeHasAnExplicitGlyph() {
        val missing = catalogTypes.filterNot { SymbolLibrary.has(it.name) }.map { it.name }
        assertTrue("Types without a glyph: $missing", missing.isEmpty())
    }

    @Test
    fun noGlyphIsDefinedForAnUnknownType() {
        val known = ComponentType.values().map { it.name }.toSet()
        val orphans = SymbolLibrary.names().filterNot { it in known }
        assertTrue("Glyphs for non-existent types: $orphans", orphans.isEmpty())
    }

    @Test
    fun everyGlyphReachesBothTerminals() {
        for (type in catalogTypes) {
            val xs = xCoordinates(type.name)
            assertTrue("${type.name} does not reach the left terminal", xs.any { it <= -23.9f })
            assertTrue("${type.name} does not reach the right terminal", xs.any { it >= 23.9f })
        }
    }

    @Test
    fun glyphsStayInsideTheComponentFootprint() {
        for (type in catalogTypes) {
            for (v in allCoordinates(type.name)) {
                assertTrue("${type.name} has out-of-range coordinate $v", v >= -26f && v <= 26f)
            }
        }
    }

    @Test
    fun parserHandlesEveryPrimitiveKind() {
        val g = GlyphParser.parse("L0,0,1,1;C0,0,2;D1,1,1;A0,0,3,0,90;R-1,-1,1,1;P0,0,1,0,1,1;F0,0,1,0,1,1;Q0,0,1,1,2,0;T0,3,8,I>")
        assertEquals(9, g.prims.size)
        assertEquals(Prim.Label(0f, 3f, 8f, "I>"), g.prims.last())
    }

    @Test
    fun unknownTypeFallsBackToLabelledBox() {
        val g = SymbolLibrary.glyphFor("DOES_NOT_EXIST", "XYZ")
        assertTrue(g.prims.any { it is Prim.Label && it.text == "XYZ" })
    }

    private fun allCoordinates(type: String): List<Float> =
        SymbolLibrary.glyphFor(type, "?").prims.flatMap { p ->
            when (p) {
                is Prim.Line -> listOf(p.x1, p.y1, p.x2, p.y2)
                is Prim.Circle -> listOf(p.cx - p.r, p.cx + p.r, p.cy - p.r, p.cy + p.r)
                is Prim.Dot -> listOf(p.cx - p.r, p.cx + p.r, p.cy - p.r, p.cy + p.r)
                is Prim.Arc -> listOf(p.cx - p.r, p.cx + p.r, p.cy - p.r, p.cy + p.r)
                is Prim.Rect -> listOf(p.l, p.t, p.r, p.b)
                is Prim.Poly -> p.pts
                is Prim.Polyline -> p.pts
                is Prim.Label -> listOf(p.x, p.y)
            }
        }

    private fun xCoordinates(type: String): List<Float> =
        SymbolLibrary.glyphFor(type, "?").prims.flatMap { p ->
            when (p) {
                is Prim.Line -> listOf(p.x1, p.x2)
                is Prim.Polyline -> p.pts.filterIndexed { i, _ -> i % 2 == 0 }
                else -> emptyList()
            }
        }
}

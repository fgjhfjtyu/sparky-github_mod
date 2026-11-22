package com.jackby03.sparky.core.symbols

/** One drawing primitive of a schematic symbol. Coordinates are relative to the component centre. */
sealed class Prim {
    data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : Prim()
    data class Circle(val cx: Float, val cy: Float, val r: Float) : Prim()
    data class Dot(val cx: Float, val cy: Float, val r: Float) : Prim()
    data class Arc(val cx: Float, val cy: Float, val r: Float, val start: Float, val sweep: Float) : Prim()
    data class Rect(val l: Float, val t: Float, val r: Float, val b: Float) : Prim()
    /** Closed polygon; [filled] polygons are used for arrow heads and solid bars. */
    data class Poly(val pts: List<Float>, val filled: Boolean) : Prim()
    data class Polyline(val pts: List<Float>) : Prim()
    data class Label(val x: Float, val y: Float, val size: Float, val text: String) : Prim()
}

data class Glyph(val prims: List<Prim>)

/** Parses the compact text encoding documented in [SymbolGlyphData]. */
object GlyphParser {

    fun parse(encoded: String): Glyph {
        val prims = ArrayList<Prim>()
        for (part in encoded.split(';')) {
            if (part.isEmpty()) continue
            val kind = part[0]
            val rest = part.substring(1)
            if (kind == 'T') {
                val f = rest.split(',', limit = 4)
                require(f.size == 4) { "Bad label: $part" }
                prims += Prim.Label(f[0].toFloat(), f[1].toFloat(), f[2].toFloat(), f[3])
                continue
            }
            val n = rest.split(',').map { it.toFloat() }
            prims += when (kind) {
                'L' -> Prim.Line(n[0], n[1], n[2], n[3])
                'C' -> Prim.Circle(n[0], n[1], n[2])
                'D' -> Prim.Dot(n[0], n[1], n[2])
                'A' -> Prim.Arc(n[0], n[1], n[2], n[3], n[4])
                'R' -> Prim.Rect(n[0], n[1], n[2], n[3])
                'P' -> Prim.Poly(n, filled = false)
                'F' -> Prim.Poly(n, filled = true)
                'Q' -> Prim.Polyline(n)
                else -> error("Unknown primitive '$kind' in: $part")
            }
        }
        return Glyph(prims)
    }
}

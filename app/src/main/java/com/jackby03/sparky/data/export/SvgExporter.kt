package com.jackby03.sparky.data.export

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.core.symbols.Prim
import com.jackby03.sparky.core.symbols.SymbolLibrary
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.SymbolComponent
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.export.SvgExportConfig
import java.io.File
import javax.inject.Inject

class SvgExporter @Inject constructor() {

    private val HALF = 24f
    private val WIRE_COLOR = "#00E5FF"
    private val COMP_COLOR = "#FFFFFF"
    private val BG_COLOR   = "#0A0A0A"
    private val LABEL_COLOR = "#AAAAAA"

    fun export(circuit: Circuit, outputFile: File, config: SvgExportConfig) {
        outputFile.writeText(buildSvg(circuit, config), Charsets.UTF_8)
    }

    private fun buildSvg(circuit: Circuit, config: SvgExportConfig): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<svg xmlns="http://www.w3.org/2000/svg" width="${config.widthMm}mm" height="${config.heightMm}mm" viewBox="0 0 ${config.widthMm} ${config.heightMm}">""")
        appendLine("""  <rect width="100%" height="100%" fill="$BG_COLOR"/>""")
        appendLine("""  <title>${circuit.name.escapeXml()}</title>""")

        // Wires
        circuit.wires.forEach { wire ->
            val start = circuit.nodeById(wire.startNodeId) ?: return@forEach
            val end   = circuit.nodeById(wire.endNodeId)   ?: return@forEach
            val points = buildString {
                append("${start.x},${start.y}")
                wire.waypoints.forEach { wp -> append(" ${wp.x},${wp.y}") }
                append(" ${end.x},${end.y}")
            }
            appendLine("""  <polyline points="$points" fill="none" stroke="$WIRE_COLOR" stroke-width="3" stroke-linecap="round"/>""")
        }

        // Components
        circuit.components.forEach { component ->
            val cx = component.x
            val cy = component.y
            val transform = if (component.rotation != 0f) """ transform="rotate(${component.rotation},$cx,$cy)"""" else ""
            appendLine("""  <g$transform>""")
            append(componentSvg(component, cx, cy))
            appendLine("""    <text x="$cx" y="${cy + HALF + 20f}" text-anchor="middle" fill="$LABEL_COLOR" font-size="12">${component.label.escapeXml()}</text>""")
            appendLine("""  </g>""")
        }

        appendLine("</svg>")
    }

    private fun componentSvg(c: CircuitComponent, cx: Float, cy: Float): String {
        val s = COMP_COLOR
        val sw = 2.5f
        return when (c) {
            is Resistor -> """
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - HALF / 2}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <rect x="${cx - HALF / 2}" y="${cy - 8}" width="${HALF}" height="16" fill="none" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + HALF / 2}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is Capacitor -> """
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - 6}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 6}" y1="${cy - 16}" x2="${cx - 6}" y2="${cy + 16}" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 6}" y1="${cy - 16}" x2="${cx + 6}" y2="${cy + 16}" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 6}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is Inductor -> """
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - HALF / 2}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <path d="M${cx - HALF / 2},$cy a8,8 0 0,1 16,0" fill="none" stroke="$s" stroke-width="$sw"/>
    <path d="M${cx - HALF / 2 + 16f},$cy a8,8 0 0,1 16,0" fill="none" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + HALF / 2}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is VoltageSource -> """
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - 16}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <circle cx="$cx" cy="$cy" r="16" fill="none" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 8}" y1="$cy" x2="${cx + 8}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="$cx" y1="${cy - 8}" x2="$cx" y2="${cy + 8}" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 16}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is CurrentSource -> """
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - 16}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <circle cx="$cx" cy="$cy" r="16" fill="none" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 6}" y1="$cy" x2="${cx + 6}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 16}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is Diode -> """
    <polygon points="${cx - 12},${cy - 12} ${cx + 12},$cy ${cx - 12},${cy + 12}" fill="none" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 12}" y1="${cy - 12}" x2="${cx + 12}" y2="${cy + 12}" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - HALF}" y1="$cy" x2="${cx - 12}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx + 12}" y1="$cy" x2="${cx + HALF}" y2="$cy" stroke="$s" stroke-width="$sw"/>
"""
            is Ground -> """
    <line x1="$cx" y1="${cy - HALF}" x2="$cx" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 16}" y1="$cy" x2="${cx + 16}" y2="$cy" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 10}" y1="${cy + 6}" x2="${cx + 10}" y2="${cy + 6}" stroke="$s" stroke-width="$sw"/>
    <line x1="${cx - 4}" y1="${cy + 12}" x2="${cx + 4}" y2="${cy + 12}" stroke="$s" stroke-width="$sw"/>
"""
            is SymbolComponent -> symbolSvg(c, cx, cy)
        }
    }

    private fun symbolSvg(c: SymbolComponent, cx: Float, cy: Float): String = buildString {
        val s = COMP_COLOR
        val sw = 2.5f
        val glyph = SymbolLibrary.glyphFor(c.componentType, c.symbol)
        fun pts(p: List<Float>): String =
            p.chunked(2).filter { it.size == 2 }.joinToString(" ") { "${cx + it[0]},${cy + it[1]}" }

        for (prim in glyph.prims) {
            when (prim) {
                is Prim.Line -> appendLine("""    <line x1="${cx + prim.x1}" y1="${cy + prim.y1}" x2="${cx + prim.x2}" y2="${cy + prim.y2}" stroke="$s" stroke-width="$sw" stroke-linecap="round"/>""")
                is Prim.Circle -> appendLine("""    <circle cx="${cx + prim.cx}" cy="${cy + prim.cy}" r="${prim.r}" fill="none" stroke="$s" stroke-width="$sw"/>""")
                is Prim.Dot -> appendLine("""    <circle cx="${cx + prim.cx}" cy="${cy + prim.cy}" r="${prim.r}" fill="$s"/>""")
                is Prim.Arc -> {
                    if (kotlin.math.abs(prim.sweep) >= 360f) {
                        appendLine("""    <circle cx="${cx + prim.cx}" cy="${cy + prim.cy}" r="${prim.r}" fill="none" stroke="$s" stroke-width="$sw"/>""")
                    } else {
                        val a0 = Math.toRadians(prim.start.toDouble())
                        val a1 = Math.toRadians((prim.start + prim.sweep).toDouble())
                        val x0 = cx + prim.cx + prim.r * kotlin.math.cos(a0).toFloat()
                        val y0 = cy + prim.cy + prim.r * kotlin.math.sin(a0).toFloat()
                        val x1 = cx + prim.cx + prim.r * kotlin.math.cos(a1).toFloat()
                        val y1 = cy + prim.cy + prim.r * kotlin.math.sin(a1).toFloat()
                        val large = if (kotlin.math.abs(prim.sweep) > 180f) 1 else 0
                        val sweepFlag = if (prim.sweep >= 0f) 1 else 0
                        appendLine("""    <path d="M$x0,$y0 A${prim.r},${prim.r} 0 $large,$sweepFlag $x1,$y1" fill="none" stroke="$s" stroke-width="$sw" stroke-linecap="round"/>""")
                    }
                }
                is Prim.Rect -> appendLine("""    <rect x="${cx + prim.l}" y="${cy + prim.t}" width="${prim.r - prim.l}" height="${prim.b - prim.t}" fill="none" stroke="$s" stroke-width="$sw"/>""")
                is Prim.Poly -> appendLine("""    <polygon points="${pts(prim.pts)}" fill="${if (prim.filled) s else "none"}" stroke="$s" stroke-width="$sw" stroke-linejoin="round"/>""")
                is Prim.Polyline -> appendLine("""    <polyline points="${pts(prim.pts)}" fill="none" stroke="$s" stroke-width="$sw" stroke-linecap="round" stroke-linejoin="round"/>""")
                is Prim.Label -> appendLine("""    <text x="${cx + prim.x}" y="${cy + prim.y}" text-anchor="middle" fill="$s" font-size="${prim.size}" font-weight="bold" font-family="sans-serif">${prim.text.escapeXml()}</text>""")
            }
        }
    }

    private fun String.escapeXml() = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

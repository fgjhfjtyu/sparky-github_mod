package com.jackby03.sparky.data.export

import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.circuit.Node
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Ground
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
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
        }
    }

    private fun String.escapeXml() = replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

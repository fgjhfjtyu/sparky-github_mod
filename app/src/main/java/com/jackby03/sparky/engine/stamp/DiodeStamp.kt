package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

/**
 * DC piecewise-linear diode model.
 *
 * Two-segment approximation:
 *   - OFF  (anode voltage ≤ Vf): open circuit — no stamp contribution.
 *   - ON   (anode voltage >  Vf): ideal forward-voltage source (Vf volts, anode to cathode).
 *
 * For DC operating-point analysis in a single-pass solver we always assume the
 * ON state (forward-biased) and model the diode as a voltage source of [forwardVoltage]
 * volts in series with zero resistance. This is correct for circuits where the diode
 * is clearly forward-biased. A Newton-Raphson iterative approach (v2.0) will handle
 * arbitrary initial conditions.
 *
 * Stamp is identical to [VoltageSourceStamp] with value = forwardVoltage.
 */
class DiodeStamp(
    private val componentId: String,
    private val anodeNodeId: String,
    private val cathodeNodeId: String,
    private val forwardVoltage: Double
) : ComponentStamp {

    override fun stamp(G: Matrix, I: DoubleArray, nodeIndex: Map<String, Int>, branchIndex: Map<String, Int>) {
        val anode   = nodeIndex[anodeNodeId]
        val cathode = nodeIndex[cathodeNodeId]
        val branch  = branchIndex[componentId] ?: return

        if (anode != null) {
            G[anode, branch]  += 1.0
            G[branch, anode]  += 1.0
        }
        if (cathode != null) {
            G[cathode, branch] -= 1.0
            G[branch, cathode] -= 1.0
        }
        I[branch] += forwardVoltage
    }
}

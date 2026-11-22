package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

/**
 * DC treatment for a capacitor: open circuit.
 *
 * In DC steady-state, a capacitor is fully charged and carries no current.
 * Its contribution to the MNA system is zero — it is simply omitted.
 * This is correct because an open circuit adds no conductance to G and no
 * current injection to I.
 */
class CapacitorStamp : ComponentStamp {
    override fun stamp(G: Matrix, I: DoubleArray, nodeIndex: Map<String, Int>, branchIndex: Map<String, Int>) {
        // Intentionally empty: capacitor = open circuit at DC steady-state.
    }
}

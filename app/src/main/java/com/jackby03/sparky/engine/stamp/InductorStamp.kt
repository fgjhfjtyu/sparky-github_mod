package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

/**
 * DC treatment for an inductor: short circuit (ideal wire, 0 V drop).
 *
 * In DC steady-state, an inductor has no reactance and behaves as a perfect
 * short. We model this as a 0 V voltage source, which adds one branch
 * variable to the MNA system (its current) and enforces V_node1 = V_node2.
 *
 * Stamp pattern for a voltage source of value 0 between node+ and node-:
 *   G[n+, branch] +=  1
 *   G[n-, branch] += -1
 *   G[branch, n+] +=  1
 *   G[branch, n-] += -1
 *   I[branch]     +=  0  (zero-volt source)
 */
class InductorStamp(
    private val componentId: String,
    private val nodeId1: String,
    private val nodeId2: String
) : ComponentStamp {

    override fun stamp(G: Matrix, I: DoubleArray, nodeIndex: Map<String, Int>, branchIndex: Map<String, Int>) {
        val n1 = nodeIndex[nodeId1]
        val n2 = nodeIndex[nodeId2]
        val branch = branchIndex[componentId] ?: return

        if (n1 != null) {
            G[n1, branch] += 1.0
            G[branch, n1] += 1.0
        }
        if (n2 != null) {
            G[n2, branch] -= 1.0
            G[branch, n2] -= 1.0
        }
        // I[branch] += 0.0 — no-op for a zero-volt source.
    }
}

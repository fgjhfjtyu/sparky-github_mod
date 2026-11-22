package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

/**
 * Independent voltage source stamp using the MNA branch-current variable.
 * Adds one extra row/column per source to the augmented matrix.
 */
class VoltageSourceStamp(
    private val componentId: String,
    private val posNodeId: String,
    private val negNodeId: String,
    private val voltageVolts: Double
) : ComponentStamp {

    override fun stamp(
        G: Matrix,
        I: DoubleArray,
        nodeIndex: Map<String, Int>,
        branchIndex: Map<String, Int>
    ) {
        val k  = branchIndex[componentId] ?: return
        val np = nodeIndex[posNodeId]
        val nn = nodeIndex[negNodeId]

        if (np != null) { G.add(np, k, 1.0);  G.add(k, np, 1.0) }
        if (nn != null) { G.add(nn, k, -1.0); G.add(k, nn, -1.0) }
        I[k] += voltageVolts
    }
}

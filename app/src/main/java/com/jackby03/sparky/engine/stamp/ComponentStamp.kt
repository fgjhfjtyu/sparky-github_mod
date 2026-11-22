package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

/**
 * Each component stamps its contribution into the MNA conductance matrix (G)
 * and current vector (I). Implementing classes must be pure and stateless.
 */
interface ComponentStamp {
    /**
     * @param G  Conductance matrix (n x n)
     * @param I  Known current vector (n)
     * @param nodeIndex  Map from node ID to matrix row index
     * @param branchIndex  Map from component ID to extra variable row (for voltage sources)
     */
    fun stamp(
        G: Matrix,
        I: DoubleArray,
        nodeIndex: Map<String, Int>,
        branchIndex: Map<String, Int>
    )
}

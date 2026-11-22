package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

class ResistorStamp(
    private val nodeId1: String,
    private val nodeId2: String,
    private val resistanceOhm: Double
) : ComponentStamp {

    override fun stamp(
        G: Matrix,
        I: DoubleArray,
        nodeIndex: Map<String, Int>,
        branchIndex: Map<String, Int>
    ) {
        val conductance = 1.0 / resistanceOhm
        val n1 = nodeIndex[nodeId1]
        val n2 = nodeIndex[nodeId2]

        if (n1 != null) G.add(n1, n1, conductance)
        if (n2 != null) G.add(n2, n2, conductance)
        if (n1 != null && n2 != null) {
            G.add(n1, n2, -conductance)
            G.add(n2, n1, -conductance)
        }
    }
}

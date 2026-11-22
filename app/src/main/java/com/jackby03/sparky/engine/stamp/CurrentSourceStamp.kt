package com.jackby03.sparky.engine.stamp

import com.jackby03.sparky.engine.model.Matrix

class CurrentSourceStamp(
    private val posNodeId: String,
    private val negNodeId: String,
    private val currentAmperes: Double
) : ComponentStamp {

    override fun stamp(
        G: Matrix,
        I: DoubleArray,
        nodeIndex: Map<String, Int>,
        branchIndex: Map<String, Int>
    ) {
        nodeIndex[posNodeId]?.let { I[it] -= currentAmperes }
        nodeIndex[negNodeId]?.let { I[it] += currentAmperes }
    }
}

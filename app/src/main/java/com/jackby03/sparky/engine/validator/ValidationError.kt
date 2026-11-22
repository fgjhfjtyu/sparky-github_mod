package com.jackby03.sparky.engine.validator

sealed class ValidationError(val message: String) {
    data object NoGround : ValidationError("Circuit has no ground node")
    data object NoComponents : ValidationError("Circuit has no components")
    data class FloatingNode(val nodeId: String) : ValidationError("Node '$nodeId' is not connected")
    data class UnconnectedTerminal(val componentLabel: String, val terminal: Int) :
        ValidationError("Component '$componentLabel' terminal $terminal is not connected")
    data class InvalidValue(val componentLabel: String, val reason: String) :
        ValidationError("Component '$componentLabel': $reason")
}

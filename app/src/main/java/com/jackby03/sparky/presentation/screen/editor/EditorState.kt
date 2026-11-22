package com.jackby03.sparky.presentation.screen.editor

import androidx.compose.ui.geometry.Offset
import com.jackby03.sparky.core.constants.AppConstants.Canvas
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.CircuitComponent
import com.jackby03.sparky.domain.model.component.ComponentType
import com.jackby03.sparky.domain.model.simulation.SimulationResult

/** Immutable state consumed by [EditorScreen] and its composables. */
data class EditorState(
    // Circuit data
    val circuit: Circuit? = null,
    val selectedComponent: CircuitComponent? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val error: String? = null,
    val undoStack: List<Circuit> = emptyList(),

    // Canvas transform (owned here so ViewModel can reset/save them)
    val canvasZoom: Float = Canvas.DEFAULT_ZOOM,
    val canvasOffset: Offset = Offset.Zero,

    // Wire drawing
    val wireMode: Boolean = false,          // true = user tapped Wire FAB, waiting for taps
    val wireDrawState: WireDrawState? = null,

    // Last simulation results (null = not yet run)
    val simulationResult: SimulationResult? = null,
    val isSimulating: Boolean = false,

    // Show unsaved changes dialog
    val showUnsavedDialog: Boolean = false,
    // Show rename dialog
    val showRenameDialog: Boolean = false,

    // Palette drag ghost — non-null while user is dragging a component type from the palette
    val componentDragType: ComponentType? = null,
    val componentDragScreenPos: Offset = Offset.Zero,

    // Snap alignment guide — non-null while dragging a component that aligns with another
    val snapGuideX: Float? = null,   // canvas-space X of a vertical alignment guide
    val snapGuideY: Float? = null,   // canvas-space Y of a horizontal alignment guide

    // Property panel visibility — toggled by tapping an already-selected component
    val showPropertyPanel: Boolean = false,

    // Clipboard — holds the last copied component for paste
    val copiedComponent: com.jackby03.sparky.domain.model.component.CircuitComponent? = null,

    // Actual pixel size of the canvas area — set once from onSizeChanged so the ViewModel
    // can compute correct ZoomToFit / ZoomToSelection without hardcoded resolutions.
    val viewportWidth: Float = 0f,
    val viewportHeight: Float = 0f,

    // Monotonic counter that only increments when the canvas transform is changed
    // externally (zoom buttons, ZoomToFit, ZoomToSelection). The canvas uses this
    // as a LaunchedEffect key to distinguish external changes from its own gesture-
    // reported updates, breaking the feedback loop that causes ghosting.
    val transformVersion: Int = 0,
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val undoCount: Int get() = undoStack.size
    val isWireDrawing: Boolean get() = wireMode || wireDrawState != null
    val isDraggingFromPalette: Boolean get() = componentDragType != null
}

/**
 * Captures the in-progress state of a wire being drawn by the user.
 *
 * @param startNodeId  ID of the node where the wire originates.
 * @param startPos     Canvas position of the start terminal (for ghost rendering).
 * @param currentPos   Current finger position on the canvas (ghost endpoint).
 */
data class WireDrawState(
    val startNodeId: String,
    val startPos: Offset,
    val currentPos: Offset
)

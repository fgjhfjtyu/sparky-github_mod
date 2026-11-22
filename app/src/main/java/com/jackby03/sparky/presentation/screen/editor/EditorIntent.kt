package com.jackby03.sparky.presentation.screen.editor

import androidx.compose.ui.geometry.Offset
import com.jackby03.sparky.domain.model.component.ComponentType

/** All actions that the editor UI can trigger on [EditorViewModel]. */
sealed class EditorIntent {
    // Component lifecycle
    data class AddComponent(val type: ComponentType, val x: Float, val y: Float) : EditorIntent()
    data class SelectComponent(val componentId: String?) : EditorIntent()
    data class MoveComponent(val componentId: String, val x: Float, val y: Float) : EditorIntent()
    data class RemoveComponent(val componentId: String) : EditorIntent()
    data class RotateComponent(val componentId: String) : EditorIntent()

    // Property editing
    data class UpdateLabel(val componentId: String, val label: String) : EditorIntent()
    data class UpdateValue(val componentId: String, val value: String) : EditorIntent()

    // Wire drawing
    data object ToggleWireMode : EditorIntent()    // Wire FAB toggle
    data class StartWire(val nodeId: String, val startPos: Offset) : EditorIntent()
    data class UpdateWireDrag(val currentPos: Offset) : EditorIntent()
    data class EndWire(val endNodeId: String) : EditorIntent()
    data object CancelWire : EditorIntent()

    // Canvas transform
    data class UpdateCanvasTransform(val zoom: Float, val offset: Offset) : EditorIntent()
    /** External transform change (zoom buttons) — bumps [EditorState.transformVersion]. */
    data class SetCanvasTransform(val zoom: Float, val offset: Offset) : EditorIntent()

    // Persistence
    data object SaveCircuit : EditorIntent()
    data object UndoLast : EditorIntent()

    // Navigation guards
    data object RequestBack : EditorIntent()
    data object ConfirmDiscard : EditorIntent()
    data object DismissUnsavedDialog : EditorIntent()

    // Rename
    data class RenameCircuit(val name: String) : EditorIntent()
    data object ShowRenameDialog : EditorIntent()
    data object DismissRenameDialog : EditorIntent()

    // Simulation
    data object RunSimulation : EditorIntent()

    // Canvas utilities
    data object ZoomToFit : EditorIntent()
    /** Zoom to the bounding box of the currently selected component. */
    data object ZoomToSelection : EditorIntent()

    // Palette drag (ghost preview while dragging a component type onto the canvas)
    data class StartComponentDrag(val type: ComponentType, val screenPos: Offset) : EditorIntent()
    data class UpdateComponentDrag(val screenPos: Offset) : EditorIntent()
    data class DropComponent(val screenPos: Offset) : EditorIntent()
    data object CancelComponentDrag : EditorIntent()

    // Snap alignment guides
    data class UpdateSnapGuides(val guideX: Float?, val guideY: Float?) : EditorIntent()

    // Viewport size — reported once from the canvas onSizeChanged so ZoomToFit is accurate
    data class UpdateViewportSize(val width: Float, val height: Float) : EditorIntent()

    // Copy / paste
    data object CopyComponent   : EditorIntent()
    data object PasteComponent  : EditorIntent()
}

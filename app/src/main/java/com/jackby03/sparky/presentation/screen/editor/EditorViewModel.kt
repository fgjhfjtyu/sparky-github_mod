package com.jackby03.sparky.presentation.screen.editor

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackby03.sparky.core.result.onError
import com.jackby03.sparky.core.result.onSuccess
import com.jackby03.sparky.core.utils.ValueParser
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.component.Capacitor
import com.jackby03.sparky.domain.model.component.CurrentSource
import com.jackby03.sparky.domain.model.component.Diode
import com.jackby03.sparky.domain.model.component.Inductor
import com.jackby03.sparky.domain.model.component.Resistor
import com.jackby03.sparky.domain.model.component.VoltageSource
import com.jackby03.sparky.domain.model.simulation.SimulationConfig
import com.jackby03.sparky.domain.usecase.circuit.AddComponentUseCase
import com.jackby03.sparky.domain.usecase.circuit.ConnectNodesUseCase
import com.jackby03.sparky.domain.usecase.circuit.LoadCircuitUseCase
import com.jackby03.sparky.domain.usecase.circuit.RemoveComponentUseCase
import com.jackby03.sparky.domain.usecase.circuit.SaveCircuitUseCase
import com.jackby03.sparky.domain.usecase.simulation.RunSimulationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadCircuit: LoadCircuitUseCase,
    private val saveCircuit: SaveCircuitUseCase,
    private val addComponent: AddComponentUseCase,
    private val removeComponent: RemoveComponentUseCase,
    private val connectNodes: ConnectNodesUseCase,
    private val runSimulation: RunSimulationUseCase
) : ViewModel() {

    private val circuitId: String = checkNotNull(savedStateHandle["circuitId"])
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    init { load() }

    fun onIntent(intent: EditorIntent) {
        val circuit = _state.value.circuit
        when (intent) {
            is EditorIntent.AddComponent          -> circuit?.let { handleAdd(it, intent) }
            is EditorIntent.SelectComponent       -> handleSelect(intent.componentId)
            is EditorIntent.MoveComponent         -> circuit?.let { handleMove(it, intent) }
            is EditorIntent.RemoveComponent       -> circuit?.let { handleRemove(it, intent.componentId) }
            is EditorIntent.RotateComponent       -> circuit?.let { handleRotate(it, intent.componentId) }
            is EditorIntent.UpdateLabel           -> circuit?.let { handleUpdateLabel(it, intent) }
            is EditorIntent.UpdateValue           -> circuit?.let { handleUpdateValue(it, intent) }
            is EditorIntent.StartWire             -> handleStartWire(intent.nodeId, intent.startPos)
            is EditorIntent.UpdateWireDrag        -> handleWireGhostMove(intent.currentPos)
            is EditorIntent.EndWire               -> circuit?.let { handleEndWire(it, intent.endNodeId) }
            is EditorIntent.ToggleWireMode         -> _state.update { it.copy(wireMode = !it.wireMode, wireDrawState = null) }
            is EditorIntent.CancelWire            -> _state.update { it.copy(wireMode = false, wireDrawState = null) }
            is EditorIntent.UpdateCanvasTransform -> _state.update { it.copy(canvasZoom = intent.zoom, canvasOffset = intent.offset) }
            is EditorIntent.SetCanvasTransform   -> _state.update { it.copy(canvasZoom = intent.zoom, canvasOffset = intent.offset, transformVersion = it.transformVersion + 1) }
            is EditorIntent.SaveCircuit           -> save()
            is EditorIntent.UndoLast              -> handleUndo()
            is EditorIntent.RequestBack           -> handleRequestBack()
            is EditorIntent.ConfirmDiscard        -> _state.update { it.copy(showUnsavedDialog = false, isDirty = false) }
            is EditorIntent.DismissUnsavedDialog  -> _state.update { it.copy(showUnsavedDialog = false) }
            is EditorIntent.ShowRenameDialog      -> _state.update { it.copy(showRenameDialog = true) }
            is EditorIntent.DismissRenameDialog   -> _state.update { it.copy(showRenameDialog = false) }
            is EditorIntent.RenameCircuit         -> circuit?.let { handleRename(it, intent.name) }
            is EditorIntent.RunSimulation         -> circuit?.let { handleRunSimulation(it) }
            is EditorIntent.ZoomToFit             -> circuit?.let { handleZoomToFit(it) }
            is EditorIntent.ZoomToSelection       -> handleZoomToSelection()
            is EditorIntent.StartComponentDrag    -> _state.update { it.copy(componentDragType = intent.type, componentDragScreenPos = intent.screenPos) }
            is EditorIntent.UpdateComponentDrag   -> _state.update { it.copy(componentDragScreenPos = intent.screenPos) }
            is EditorIntent.DropComponent         -> circuit?.let { handleDropComponent(it, intent.screenPos) }
            is EditorIntent.CancelComponentDrag   -> _state.update { it.copy(componentDragType = null) }
            is EditorIntent.UpdateSnapGuides      -> _state.update { it.copy(snapGuideX = intent.guideX, snapGuideY = intent.guideY) }
            is EditorIntent.UpdateViewportSize    -> _state.update { it.copy(viewportWidth = intent.width, viewportHeight = intent.height) }
            is EditorIntent.CopyComponent         -> handleCopy()
            is EditorIntent.PasteComponent        -> circuit?.let { handlePaste(it) }
        }
    }

    // ── Load / Save ───────────────────────────────────────────────────────────

    private fun load() {
        viewModelScope.launch {
            loadCircuit(circuitId)
                .onSuccess { c -> _state.update { it.copy(circuit = c, isLoading = false) } }
                .onError { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    private fun save() {
        val circuit = _state.value.circuit ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            saveCircuit(circuit)
                .onSuccess { _state.update { it.copy(isSaving = false, isDirty = false) } }
                .onError { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    // ── Component operations ──────────────────────────────────────────────────

    private fun handleAdd(circuit: Circuit, intent: EditorIntent.AddComponent) {
        val component = intent.type.createDefault().withPosition(intent.x, intent.y)
        addComponent(circuit, component, intent.type)
            .onSuccess { updated -> updateCircuit(circuit, updated) }
    }

    private fun handleSelect(componentId: String?) {
        val current = _state.value.selectedComponent
        val component = _state.value.circuit?.components?.find { it.id == componentId }
        if (componentId != null && current?.id == componentId) {
            // Tap on already-selected component → toggle property panel
            _state.update { it.copy(showPropertyPanel = !it.showPropertyPanel) }
        } else {
            // New selection or deselect — hide property panel
            _state.update { it.copy(selectedComponent = component, showPropertyPanel = false) }
        }
    }

    private fun handleMove(circuit: Circuit, intent: EditorIntent.MoveComponent) {
        val updated = circuit.copy(
            components = circuit.components.map { c ->
                if (c.id == intent.componentId) c.withPosition(intent.x, intent.y) else c
            },
            nodes = circuit.nodes.map { node ->
                // Keep terminal nodes aligned with component position
                val ownerComponent = circuit.components.find { comp ->
                    comp.terminalNodeIds.contains(node.id)
                }
                if (ownerComponent?.id == intent.componentId) {
                    val movedComponent = circuit.components
                        .find { it.id == intent.componentId }
                        ?.withPosition(intent.x, intent.y) ?: return@map node
                    val (t0, t1) = terminalNodesOf(movedComponent, node.id,
                        ownerComponent.terminalNodeIds)
                    node.copy(x = t0, y = t1)
                } else node
            }
        )
        updateCircuit(circuit, updated)
    }

    private fun terminalNodesOf(
        component: com.jackby03.sparky.domain.model.component.CircuitComponent,
        nodeId: String,
        terminalIds: List<String>
    ): Pair<Float, Float> {
        val half = 24f
        return when {
            terminalIds.getOrNull(0) == nodeId -> component.x - half to component.y
            terminalIds.getOrNull(1) == nodeId -> component.x + half to component.y
            else -> component.x to component.y
        }
    }

    private fun handleRemove(circuit: Circuit, componentId: String) {
        removeComponent(circuit, componentId)
            .onSuccess { updated ->
                if (_state.value.selectedComponent?.id == componentId) {
                    _state.update { it.copy(selectedComponent = null) }
                }
                updateCircuit(circuit, updated)
            }
    }

    private fun handleRotate(circuit: Circuit, componentId: String) {
        val updated = circuit.copy(
            components = circuit.components.map { c ->
                if (c.id == componentId) c.withRotation((c.rotation + 90f) % 360f) else c
            }
        )
        updateCircuit(circuit, updated)
    }

    private fun handleUpdateLabel(circuit: Circuit, intent: EditorIntent.UpdateLabel) {
        val updated = circuit.copy(
            components = circuit.components.map { c ->
                if (c.id == intent.componentId) c.withLabel(intent.label) else c
            }
        )
        updateCircuit(circuit, updated)
    }

    private fun handleUpdateValue(circuit: Circuit, intent: EditorIntent.UpdateValue) {
        val parsed = ValueParser.parse(intent.value) ?: return
        val updated = circuit.copy(
            components = circuit.components.map { c ->
                if (c.id != intent.componentId) return@map c
                when (c) {
                    is Resistor      -> c.copy(resistanceOhm      = parsed)
                    is Capacitor     -> c.copy(capacitanceFarad   = parsed)
                    is Inductor      -> c.copy(inductanceHenry    = parsed)
                    is VoltageSource -> c.copy(voltageVolts       = parsed)
                    is CurrentSource -> c.copy(currentAmperes     = parsed)
                    is Diode         -> c.copy(forwardVoltageVolts = parsed)
                    else             -> c
                }
            }
        )
        updateCircuit(circuit, updated)
    }

    // ── Wire drawing ──────────────────────────────────────────────────────────

    private fun handleStartWire(nodeId: String, startPos: Offset) {
        _state.update {
            it.copy(wireDrawState = WireDrawState(
                startNodeId = nodeId,
                startPos    = startPos,
                currentPos  = startPos
            ))
        }
    }

    private fun handleWireGhostMove(currentPos: Offset) {
        val existing = _state.value.wireDrawState ?: return
        _state.update { it.copy(wireDrawState = existing.copy(currentPos = currentPos)) }
    }

    private fun handleEndWire(circuit: Circuit, endNodeId: String) {
        val wireState = _state.value.wireDrawState ?: return
        if (wireState.startNodeId == endNodeId) {
            _state.update { it.copy(wireDrawState = null) }
            return
        }
        connectNodes(circuit, wireState.startNodeId, endNodeId)
            .onSuccess { updated ->
                _state.update { it.copy(wireDrawState = null) }
                updateCircuit(circuit, updated)
            }
            .onError { _state.update { it.copy(wireDrawState = null) } }
    }

    // ── Simulation ────────────────────────────────────────────────────────────

    private fun handleRunSimulation(circuit: Circuit) {
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isSimulating = true, simulationResult = null) }
            try {
                withTimeout(SIMULATION_TIMEOUT_MS) {
                    runSimulation(circuit, SimulationConfig())
                        .onSuccess { result -> _state.update { it.copy(isSimulating = false, simulationResult = result) } }
                        .onError { e -> _state.update { it.copy(isSimulating = false, error = e.message) } }
                }
            } catch (_: TimeoutCancellationException) {
                _state.update { it.copy(isSimulating = false, error = "Simulation timed out (> 5 s). Simplify the circuit.") }
            }
        }
    }

    // ── Palette drag drop ─────────────────────────────────────────────────────

    private fun handleDropComponent(circuit: Circuit, screenPos: Offset) {
        val type = _state.value.componentDragType ?: return
        _state.update { it.copy(componentDragType = null) }

        val zoom   = _state.value.canvasZoom
        val offset = _state.value.canvasOffset
        val canvasX = (screenPos.x - offset.x) / zoom
        val canvasY = (screenPos.y - offset.y) / zoom

        val component = type.createDefault().withPosition(canvasX, canvasY)
        addComponent(circuit, component, type)
            .onSuccess { updated -> updateCircuit(circuit, updated) }
    }

    // ── Copy / Paste ──────────────────────────────────────────────────────────

    private fun handleCopy() {
        val component = _state.value.selectedComponent ?: return
        _state.update { it.copy(copiedComponent = component) }
    }

    private fun handlePaste(circuit: Circuit) {
        val template = _state.value.copiedComponent ?: return
        val type = when (template) {
            is com.jackby03.sparky.domain.model.component.Resistor      -> com.jackby03.sparky.domain.model.component.ComponentType.RESISTOR
            is com.jackby03.sparky.domain.model.component.Capacitor     -> com.jackby03.sparky.domain.model.component.ComponentType.CAPACITOR
            is com.jackby03.sparky.domain.model.component.Inductor      -> com.jackby03.sparky.domain.model.component.ComponentType.INDUCTOR
            is com.jackby03.sparky.domain.model.component.VoltageSource -> com.jackby03.sparky.domain.model.component.ComponentType.VOLTAGE_SOURCE
            is com.jackby03.sparky.domain.model.component.CurrentSource -> com.jackby03.sparky.domain.model.component.ComponentType.CURRENT_SOURCE
            is com.jackby03.sparky.domain.model.component.Diode         -> com.jackby03.sparky.domain.model.component.ComponentType.DIODE
            is com.jackby03.sparky.domain.model.component.Ground        -> com.jackby03.sparky.domain.model.component.ComponentType.GROUND
        }
        val nextLabel = com.jackby03.sparky.core.utils.AutoNumbering.nextLabel(type, circuit.components)
        // Offset the pasted component by 32 canvas units so it doesn't overlap the original
        val pastedComponent = template
            .withPosition(template.x + 32f, template.y + 32f)
            .withLabel(nextLabel)
        addComponent(circuit, pastedComponent, type)
            .onSuccess { updated -> updateCircuit(circuit, updated) }
    }

    // ── Zoom-to-selection ─────────────────────────────────────────────────────

    private fun handleZoomToSelection() {
        val component = _state.value.selectedComponent ?: return
        val vpW = _state.value.viewportWidth.takeIf { it > 0f } ?: return
        val vpH = _state.value.viewportHeight.takeIf { it > 0f } ?: return
        val padding = 120f
        val minX = component.x - 48f; val maxX = component.x + 48f
        val minY = component.y - 48f; val maxY = component.y + 48f
        val contentW = (maxX - minX).coerceAtLeast(1f)
        val contentH = (maxY - minY).coerceAtLeast(1f)
        val fitZoom = ((vpW - padding * 2) / contentW)
            .coerceAtMost((vpH - padding * 2) / contentH)
            .coerceIn(0.5f, 2f)
        val offsetX = vpW / 2f - (minX + contentW / 2f) * fitZoom
        val offsetY = vpH / 2f - (minY + contentH / 2f) * fitZoom
        _state.update { it.copy(canvasZoom = fitZoom, canvasOffset = Offset(offsetX, offsetY), transformVersion = it.transformVersion + 1) }
    }

    // ── Zoom-to-fit ───────────────────────────────────────────────────────────

    private fun handleZoomToFit(circuit: Circuit) {
        val vpW = _state.value.viewportWidth.takeIf { it > 0f } ?: return
        val vpH = _state.value.viewportHeight.takeIf { it > 0f } ?: return
        if (circuit.components.isEmpty()) return
        val padding = 64f

        val minX = circuit.components.minOf { it.x } - 48f
        val minY = circuit.components.minOf { it.y } - 48f
        val maxX = circuit.components.maxOf { it.x } + 48f
        val maxY = circuit.components.maxOf { it.y } + 48f
        val contentW = (maxX - minX).coerceAtLeast(1f)
        val contentH = (maxY - minY).coerceAtLeast(1f)

        val fitZoom = ((vpW - padding * 2) / contentW)
            .coerceAtMost((vpH - padding * 2) / contentH)
            .coerceIn(0.25f, 1.5f)  // cap at 150% — fit-all should never be zoomed in too much

        val offsetX = vpW / 2f - (minX + contentW / 2f) * fitZoom
        val offsetY = vpH / 2f - (minY + contentH / 2f) * fitZoom
        _state.update { it.copy(canvasZoom = fitZoom, canvasOffset = Offset(offsetX, offsetY), transformVersion = it.transformVersion + 1) }
    }

    // ── Navigation guards ─────────────────────────────────────────────────────

    private fun handleRequestBack() {
        if (_state.value.isDirty) {
            _state.update { it.copy(showUnsavedDialog = true) }
        }
        // If not dirty, the Screen handles actual navigation
    }

    private fun handleRename(circuit: Circuit, name: String) {
        val updated = circuit.copy(name = name.trim().ifEmpty { circuit.name })
        _state.update { it.copy(showRenameDialog = false) }
        updateCircuit(circuit, updated)
    }

    // ── Undo ──────────────────────────────────────────────────────────────────

    private fun handleUndo() {
        val stack = _state.value.undoStack
        if (stack.isEmpty()) return
        _state.update { it.copy(circuit = stack.last(), undoStack = stack.dropLast(1), isDirty = true) }
    }

    // ── State mutation ────────────────────────────────────────────────────────

    private fun updateCircuit(previous: Circuit, updated: Circuit) {
        _state.update { state ->
            state.copy(
                circuit   = updated,
                isDirty   = true,
                undoStack = (state.undoStack + previous).takeLast(MAX_UNDO_STEPS)
            )
        }
    }

    companion object {
        private const val MAX_UNDO_STEPS = 20
        private const val SIMULATION_TIMEOUT_MS = 5_000L
    }
}

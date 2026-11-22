package com.jackby03.sparky.presentation.screen.editor

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jackby03.sparky.core.constants.AppConstants.Canvas as CanvasConst
import com.jackby03.sparky.domain.model.component.ComponentType
import com.jackby03.sparky.presentation.components.canvas.CircuitCanvas
import com.jackby03.sparky.presentation.components.common.PropertyPanel
import com.jackby03.sparky.presentation.components.toolbar.ComponentPalette
import com.jackby03.sparky.presentation.components.toolbar.ZoomControls
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    circuitId: String,
    onRunSimulation: () -> Unit,
    onExport: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isExpandedLayout = LocalConfiguration.current.screenWidthDp >= 600
    val focusRequester = remember { FocusRequester() }

    // Request focus so keyboard shortcuts work on tablets with physical keyboards
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Navigate back when not dirty (dialog confirmed or was never dirty)
    LaunchedEffect(state.showUnsavedDialog, state.isDirty) {
        // Navigation is triggered by the RequestBack intent; handled in the screen
    }

    // Unsaved changes dialog
    if (state.showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(EditorIntent.DismissUnsavedDialog) },
            title   = { Text("Unsaved changes") },
            text    = { Text("Leave without saving?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onIntent(EditorIntent.ConfirmDiscard)
                    onBack()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(EditorIntent.DismissUnsavedDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename dialog
    if (state.showRenameDialog) {
        var nameInput by remember(state.circuit?.name) { mutableStateOf(state.circuit?.name ?: "") }
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(EditorIntent.DismissRenameDialog) },
            title   = { Text("Rename circuit") },
            text    = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(EditorIntent.RenameCircuit(nameInput)) }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(EditorIntent.DismissRenameDialog) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.circuit?.name ?: "Editor")
                        IconButton(onClick = { viewModel.onIntent(EditorIntent.ShowRenameDialog) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Rename",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isDirty) viewModel.onIntent(EditorIntent.RequestBack)
                        else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Undo with count badge
                    BadgedBox(badge = {
                        if (state.undoCount > 0) Badge { Text("${state.undoCount}") }
                    }) {
                        IconButton(
                            onClick  = { viewModel.onIntent(EditorIntent.UndoLast) },
                            enabled  = state.canUndo
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo (${state.undoCount})")
                        }
                    }
                    // Zoom-to-selection (only when a component is selected)
                    if (state.selectedComponent != null) {
                        IconButton(onClick = { viewModel.onIntent(EditorIntent.ZoomToSelection) }) {
                            Icon(Icons.Filled.FilterCenterFocus, contentDescription = "Zoom to selection")
                        }
                    }
                    // Run simulation
                    IconButton(onClick = {
                        viewModel.onIntent(EditorIntent.RunSimulation)
                        onRunSimulation()
                    }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Run simulation",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    // Save
                    IconButton(onClick = { viewModel.onIntent(EditorIntent.SaveCircuit) }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save",
                            tint = if (state.isDirty) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        // Del / Backspace → delete selected component
                        (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace)
                                && !keyEvent.isCtrlPressed -> {
                            state.selectedComponent?.let {
                                viewModel.onIntent(EditorIntent.RemoveComponent(it.id))
                            } != null
                        }
                        // Ctrl+Z → undo
                        keyEvent.isCtrlPressed && keyEvent.key == Key.Z -> {
                            viewModel.onIntent(EditorIntent.UndoLast); true
                        }
                        // Ctrl+S → save
                        keyEvent.isCtrlPressed && keyEvent.key == Key.S -> {
                            viewModel.onIntent(EditorIntent.SaveCircuit); true
                        }
                        // Ctrl+C → copy selected component
                        keyEvent.isCtrlPressed && keyEvent.key == Key.C -> {
                            viewModel.onIntent(EditorIntent.CopyComponent); true
                        }
                        // Ctrl+V → paste copied component
                        keyEvent.isCtrlPressed && keyEvent.key == Key.V -> {
                            viewModel.onIntent(EditorIntent.PasteComponent); true
                        }
                        else -> false
                    }
                }
        ) {
            if (isExpandedLayout) {
                TabletLayout(
                    state    = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PhoneLayout(
                    state    = state,
                    onIntent = viewModel::onIntent,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Ghost overlay — follows finger while dragging from palette
            if (state.isDraggingFromPalette) {
                DragGhostOverlay(
                    type      = state.componentDragType!!,
                    screenPos = state.componentDragScreenPos
                )
            }
        }
    }
}

/** Floating badge that follows the finger during a palette drag. */
@Composable
private fun DragGhostOverlay(type: com.jackby03.sparky.domain.model.component.ComponentType, screenPos: Offset) {
    Surface(
        modifier = Modifier
            .offset { IntOffset((screenPos.x - 40).roundToInt(), (screenPos.y - 40).roundToInt()) }
            .size(80.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(type.symbol, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary)
            Text(type.displayName, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// ── Tablet: side rail palette | canvas | property panel ──────────────────────

@Composable
private fun TabletLayout(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Row(modifier = modifier.fillMaxSize()) {
        ComponentPalette(
            onComponentSelected = { type ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onIntent(EditorIntent.AddComponent(type, 300f, 300f))
            },
            onDragStart  = { type, pos -> onIntent(EditorIntent.StartComponentDrag(type, pos)) },
            onDragUpdate = { pos -> onIntent(EditorIntent.UpdateComponentDrag(pos)) },
            onDragEnd    = { pos -> onIntent(EditorIntent.DropComponent(pos)) },
            onDragCancel = { onIntent(EditorIntent.CancelComponentDrag) }
        )
        Box(modifier = Modifier.weight(1f)) {
            CanvasArea(state = state, onIntent = onIntent)
        }
        state.selectedComponent?.let { component ->
            PropertyPanel(
                component    = component,
                onLabelChange = { onIntent(EditorIntent.UpdateLabel(component.id, it)) },
                onValueChange = { onIntent(EditorIntent.UpdateValue(component.id, it)) },
                onRotate     = { onIntent(EditorIntent.RotateComponent(component.id)) },
                onDelete     = { onIntent(EditorIntent.RemoveComponent(component.id)) },
                modifier     = Modifier.padding(8.dp)
            )
        }
    }
}

// ── Phone: full-screen canvas + FAB palette dialog ───────────────────────────

// Height of the WireFAB (ExtendedFAB standard = 56dp) + its bottom margin (16dp).
// Used to lift ZoomControls above the FAB so they don't overlap.
private val WIRE_FAB_CLEARANCE = 72.dp

@Composable
private fun PhoneLayout(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddComponentDialog(
            onDismiss = { showAddDialog = false },
            onComponentSelected = { type ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showAddDialog = false
                val cx = (-state.canvasOffset.x + state.viewportWidth  / 2f) / state.canvasZoom
                val cy = (-state.canvasOffset.y + state.viewportHeight / 2f) / state.canvasZoom
                onIntent(EditorIntent.AddComponent(type, cx, cy))
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Canvas — fills the entire safe area (Scaffold already insets for TopBar + NavBar)
        CanvasArea(state = state, onIntent = onIntent, fabClearance = WIRE_FAB_CLEARANCE)

        // Wire FAB — bottom-end. Toggles wire mode on/off.
        ExtendedFloatingActionButton(
            onClick = {
                if (state.isWireDrawing) onIntent(EditorIntent.CancelWire)
                else onIntent(EditorIntent.ToggleWireMode)
            },
            icon = {
                Icon(
                    if (state.isWireDrawing) Icons.Filled.Close else Icons.Filled.Cable,
                    contentDescription = if (state.isWireDrawing) "Cancel wire" else "Draw wire"
                )
            },
            text = { Text(if (state.isWireDrawing) "Cancel" else "Wire") },
            containerColor = if (state.isWireDrawing) MaterialTheme.colorScheme.error
                             else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        )

        // Add-component FAB — bottom-start, clear of WireFAB on the right
        FloatingActionButton(
            onClick        = { showAddDialog = true },
            modifier       = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                imageVector        = Icons.Filled.Add,
                contentDescription = "Add component",
                tint               = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Property panel — slides up from bottom when user taps the already-selected
        // component (second tap). First tap only shows the selection ring.
        if (state.showPropertyPanel) state.selectedComponent?.let { component ->
            Surface(
                modifier        = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start  = 72.dp,             // clear AddFAB (56dp FAB + 16dp margin)
                        end    = 16.dp,
                        bottom = WIRE_FAB_CLEARANCE + 8.dp
                    ),
                shape           = MaterialTheme.shapes.medium,
                tonalElevation  = 4.dp,
                shadowElevation = 4.dp
            ) {
                PropertyPanel(
                    component     = component,
                    onLabelChange = { onIntent(EditorIntent.UpdateLabel(component.id, it)) },
                    onValueChange = { onIntent(EditorIntent.UpdateValue(component.id, it)) },
                    onRotate      = { onIntent(EditorIntent.RotateComponent(component.id)) },
                    onDelete      = { onIntent(EditorIntent.RemoveComponent(component.id)) }
                )
            }
        }
    }
}

// ── Shared canvas area ────────────────────────────────────────────────────────

@Composable
private fun CanvasArea(
    state: EditorState,
    onIntent: (EditorIntent) -> Unit,
    fabClearance: Dp = 0.dp
) {
    val circuit = state.circuit ?: return
    val haptic = LocalHapticFeedback.current
    Box(modifier = Modifier.fillMaxSize().onSizeChanged { size ->
        // Report real pixel size to ViewModel so ZoomToFit uses actual dimensions
        onIntent(EditorIntent.UpdateViewportSize(size.width.toFloat(), size.height.toFloat()))
    }) {
        CircuitCanvas(
            circuit               = circuit,
            selectedComponentId   = state.selectedComponent?.id,
            wireMode              = state.wireMode,
            wireDrawState         = state.wireDrawState,
            simulationResult      = state.simulationResult,
            snapGuideX            = state.snapGuideX,
            snapGuideY            = state.snapGuideY,
            onComponentSelected   = { onIntent(EditorIntent.SelectComponent(it)) },
            onComponentMoved      = { id, x, y -> onIntent(EditorIntent.MoveComponent(id, x, y)) },
            onCanvasTransformChanged = { zoom, offset -> onIntent(EditorIntent.UpdateCanvasTransform(zoom, offset)) },
            onSnapGuidesChanged   = { gx, gy -> onIntent(EditorIntent.UpdateSnapGuides(gx, gy)) },
            onTapNode             = { nodeId, pos ->
                if (state.wireDrawState == null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIntent(EditorIntent.StartWire(nodeId, pos))
                } else {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onIntent(EditorIntent.EndWire(nodeId))
                }
            },
            onWireGhostMoved      = { onIntent(EditorIntent.UpdateWireDrag(it)) },
            initialZoom           = state.canvasZoom,
            initialOffset         = state.canvasOffset,
            transformVersion      = state.transformVersion,
            modifier              = Modifier.fillMaxSize()
        )
        ZoomControls(
            zoom        = state.canvasZoom,
            onZoomIn    = {
                val newZoom = (state.canvasZoom * 1.25f).coerceAtMost(CanvasConst.MAX_ZOOM)
                val cx = state.viewportWidth  / 2f
                val cy = state.viewportHeight / 2f
                val ratio = newZoom / state.canvasZoom
                val newOffset = Offset(
                    cx + (state.canvasOffset.x - cx) * ratio,
                    cy + (state.canvasOffset.y - cy) * ratio
                )
                onIntent(EditorIntent.SetCanvasTransform(newZoom, newOffset))
            },
            onZoomOut   = {
                val newZoom = (state.canvasZoom / 1.25f).coerceAtLeast(CanvasConst.MIN_ZOOM)
                val cx = state.viewportWidth  / 2f
                val cy = state.viewportHeight / 2f
                val ratio = newZoom / state.canvasZoom
                val newOffset = Offset(
                    cx + (state.canvasOffset.x - cx) * ratio,
                    cy + (state.canvasOffset.y - cy) * ratio
                )
                onIntent(EditorIntent.SetCanvasTransform(newZoom, newOffset))
            },
            onZoomToFit = { onIntent(EditorIntent.ZoomToFit) },
            modifier    = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp + fabClearance)
        )
    }
}

// ── Add-component dialog ──────────────────────────────────────────────────────

@Composable
private fun AddComponentDialog(
    onDismiss: () -> Unit,
    onComponentSelected: (ComponentType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Add component") },
        text    = {
            LazyVerticalGrid(
                columns                  = GridCells.Fixed(3),
                horizontalArrangement    = Arrangement.spacedBy(8.dp),
                verticalArrangement      = Arrangement.spacedBy(8.dp),
                modifier                 = Modifier.fillMaxWidth()
            ) {
                items(ComponentType.entries) { type ->
                    Surface(
                        onClick  = { onComponentSelected(type) },
                        shape    = MaterialTheme.shapes.medium,
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Column(
                            modifier             = Modifier.padding(8.dp),
                            horizontalAlignment  = Alignment.CenterHorizontally,
                            verticalArrangement  = Arrangement.Center
                        ) {
                            Text(
                                text  = type.symbol,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text      = type.displayName,
                                style     = MaterialTheme.typography.labelSmall,
                                color     = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

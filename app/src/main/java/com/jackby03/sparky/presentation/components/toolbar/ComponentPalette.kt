package com.jackby03.sparky.presentation.components.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jackby03.sparky.domain.model.component.ComponentType

@Composable
fun ComponentPalette(
    onComponentSelected: (ComponentType) -> Unit,
    onDragStart: (ComponentType, Offset) -> Unit = { _, _ -> },
    onDragUpdate: (Offset) -> Unit = { _ -> },
    onDragEnd: (Offset) -> Unit = { _ -> },
    onDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(80.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(ComponentType.entries) { type ->
                PaletteItem(
                    type       = type,
                    onClick    = { onComponentSelected(type) },
                    onDragStart  = { pos -> onDragStart(type, pos) },
                    onDragUpdate = onDragUpdate,
                    onDragEnd    = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
        }
    }
}

@Composable
private fun PaletteItem(
    type: ComponentType,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    onDragCancel: () -> Unit
) {
    var windowPos by remember { mutableStateOf(Offset.Zero) }
    var dragPos   by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .heightIn(min = 48.dp)   // WCAG / Material 3 minimum touch target
            .onGloballyPositioned { coords ->
                windowPos = coords.positionInWindow()
            }
            .clickable(onClick = onClick)
            .pointerInput(type) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        dragPos = windowPos + localOffset
                        onDragStart(dragPos)
                    },
                    onDrag = { change, _ ->
                        dragPos = windowPos + change.position
                        onDragUpdate(dragPos)
                    },
                    onDragEnd    = { onDragEnd(dragPos) },
                    onDragCancel = { onDragCancel() }
                )
            }
            .padding(8.dp)
            .semantics { contentDescription = "Palette item: ${type.displayName}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = type.symbol,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = type.category,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

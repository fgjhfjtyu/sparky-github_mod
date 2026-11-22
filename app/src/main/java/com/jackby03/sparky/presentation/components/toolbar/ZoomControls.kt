package com.jackby03.sparky.presentation.components.toolbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun ZoomControls(
    zoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomToFit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.semantics { contentDescription = "Zoom in" }
            ) {
                Icon(Icons.Filled.Add, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = "${(zoom * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.semantics { contentDescription = "Zoom out" }
            ) {
                Icon(Icons.Filled.Remove, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface)
            }
            if (onZoomToFit != null) {
                IconButton(
                    onClick = onZoomToFit,
                    modifier = Modifier.semantics { contentDescription = "Zoom to fit all components" }
                ) {
                    Icon(Icons.Filled.FitScreen, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

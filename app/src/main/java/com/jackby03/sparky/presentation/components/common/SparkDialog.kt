package com.jackby03.sparky.presentation.components.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SparkConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirm",
    dismissLabel: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text  = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { SparkButton(text = confirmLabel, onClick = onConfirm) },
        dismissButton = { SparkOutlinedButton(text = dismissLabel, onClick = onDismiss) },
        modifier = modifier
    )
}

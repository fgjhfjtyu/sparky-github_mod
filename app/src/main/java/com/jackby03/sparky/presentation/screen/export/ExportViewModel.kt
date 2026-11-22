package com.jackby03.sparky.presentation.screen.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackby03.sparky.core.result.onError
import com.jackby03.sparky.core.result.onSuccess
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.ExportConfig
import com.jackby03.sparky.domain.model.export.ExportFormat
import com.jackby03.sparky.domain.usecase.circuit.LoadCircuitUseCase
import com.jackby03.sparky.domain.usecase.export.ExportToImageUseCase
import com.jackby03.sparky.domain.usecase.export.ExportToPdfUseCase
import com.jackby03.sparky.domain.usecase.export.ExportToSvgUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ExportState(
    val circuit: Circuit? = null,
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val isExporting: Boolean = false,
    val exportedFile: File? = null,
    val error: String? = null
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context,
    private val loadCircuit: LoadCircuitUseCase,
    private val exportToPdf: ExportToPdfUseCase,
    private val exportToImage: ExportToImageUseCase,
    private val exportToSvg: ExportToSvgUseCase
) : ViewModel() {

    private val circuitId: String = checkNotNull(savedStateHandle["circuitId"])
    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            loadCircuit(circuitId).onSuccess { c -> _state.update { it.copy(circuit = c) } }
        }
    }

    fun selectFormat(format: ExportFormat) = _state.update { it.copy(selectedFormat = format) }

    fun shareExportedFile(context: Context) {
        val file = _state.value.exportedFile ?: return
        val format = _state.value.selectedFormat
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } catch (_: Exception) { /* file may not exist */ }
    }

    fun saveToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val file = _state.value.exportedFile ?: return@launch
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Save failed: ${e.message}") }
            }
        }
    }

    fun startExport() {
        val circuit = _state.value.circuit ?: return
        _state.update { it.copy(isExporting = true, error = null) }

        viewModelScope.launch {
            val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
            val format = _state.value.selectedFormat
            val file = File(outputDir, "${circuit.name}.${format.extension}")
            val config = ExportConfig(format = format)

            val result = when (format) {
                ExportFormat.PDF  -> exportToPdf(circuit, file, config.pdfConfig)
                ExportFormat.PNG,
                ExportFormat.JPEG -> exportToImage(circuit, file, config.imageConfig)
                ExportFormat.SVG,
                ExportFormat.DXF  -> exportToSvg(circuit, file, config.svgConfig)
            }

            result
                .onSuccess { f -> _state.update { it.copy(isExporting = false, exportedFile = f) } }
                .onError { e -> _state.update { it.copy(isExporting = false, error = e.message) } }
        }
    }
}

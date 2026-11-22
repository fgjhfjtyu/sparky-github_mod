package com.jackby03.sparky.domain.usecase.export

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.data.export.PdfExporter
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.PdfExportConfig
import java.io.File
import javax.inject.Inject

class ExportToPdfUseCase @Inject constructor(
    private val exporter: PdfExporter
) {
    suspend operator fun invoke(circuit: Circuit, outputFile: File, config: PdfExportConfig): AppResult<File> =
        try {
            exporter.export(circuit, outputFile, config)
            AppResult.Success(outputFile)
        } catch (e: Exception) {
            AppResult.Error(AppException.ExportException("PDF export failed", e))
        }
}

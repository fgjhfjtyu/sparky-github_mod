package com.jackby03.sparky.domain.usecase.export

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.data.export.ImageExporter
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.ImageExportConfig
import java.io.File
import javax.inject.Inject

class ExportToImageUseCase @Inject constructor(
    private val exporter: ImageExporter
) {
    suspend operator fun invoke(circuit: Circuit, outputFile: File, config: ImageExportConfig): AppResult<File> =
        try {
            exporter.export(circuit, outputFile, config)
            AppResult.Success(outputFile)
        } catch (e: Exception) {
            AppResult.Error(AppException.ExportException("Image export failed", e))
        }
}

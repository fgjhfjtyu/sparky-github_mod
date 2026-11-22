package com.jackby03.sparky.domain.usecase.export

import com.jackby03.sparky.core.result.AppException
import com.jackby03.sparky.core.result.AppResult
import com.jackby03.sparky.data.export.SvgExporter
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.SvgExportConfig
import java.io.File
import javax.inject.Inject

class ExportToSvgUseCase @Inject constructor(
    private val exporter: SvgExporter
) {
    suspend operator fun invoke(circuit: Circuit, outputFile: File, config: SvgExportConfig): AppResult<File> =
        try {
            exporter.export(circuit, outputFile, config)
            AppResult.Success(outputFile)
        } catch (e: Exception) {
            AppResult.Error(AppException.ExportException("SVG export failed", e))
        }
}

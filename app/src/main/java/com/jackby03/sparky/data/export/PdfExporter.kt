package com.jackby03.sparky.data.export

import android.graphics.pdf.PdfDocument
import com.jackby03.sparky.core.constants.AppConstants.Export
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.PageSize
import com.jackby03.sparky.domain.model.export.PdfExportConfig
import java.io.File
import javax.inject.Inject

class PdfExporter @Inject constructor() {

    /**
     * Renders [circuit] into a single-page PDF at 72 dpi (screen resolution)
     * and writes the result to [outputFile].
     */
    fun export(circuit: Circuit, outputFile: File, config: PdfExportConfig) {
        val pdf = PdfDocument()
        val (widthPx, heightPx) = config.pageSize.toPixels(dpi = 72)

        val pageInfo = PdfDocument.PageInfo.Builder(widthPx, heightPx, 1).create()
        val page = pdf.startPage(pageInfo)

        val title = if (config.includeTitle) circuit.name else null
        CircuitRenderer.render(page.canvas, circuit, widthPx, heightPx, title)

        pdf.finishPage(page)
        outputFile.outputStream().use { pdf.writeTo(it) }
        pdf.close()
    }

    private fun PageSize.toPixels(dpi: Int): Pair<Int, Int> {
        val mmPerInch = 25.4f
        return (widthMm / mmPerInch * dpi).toInt() to (heightMm / mmPerInch * dpi).toInt()
    }
}

package com.jackby03.sparky.domain.model.export

import com.jackby03.sparky.core.constants.AppConstants.Export

data class ExportConfig(
    val format: ExportFormat,
    val imageConfig: ImageExportConfig = ImageExportConfig(),
    val pdfConfig: PdfExportConfig = PdfExportConfig(),
    val svgConfig: SvgExportConfig = SvgExportConfig()
)

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF Document",  "pdf", "application/pdf"),
    PNG("PNG Image",     "png", "image/png"),
    JPEG("JPEG Image",   "jpg", "image/jpeg"),
    SVG("SVG Vector",    "svg", "image/svg+xml"),
    DXF("DXF CAD",       "dxf", "application/octet-stream")
}

data class ImageExportConfig(
    val widthPx: Int = 1920,
    val heightPx: Int = 1080,
    val quality: Int = Export.DEFAULT_IMAGE_QUALITY,
    val dpi: Int = Export.DEFAULT_DPI
)

data class PdfExportConfig(
    val pageSize: PageSize = PageSize.A4,
    val marginMm: Float = Export.PDF_MARGIN_MM,
    val includeSimulationData: Boolean = true,
    val includeTitle: Boolean = true
)

data class SvgExportConfig(
    val widthMm: Float = 297f,
    val heightMm: Float = 210f
)

enum class PageSize(val widthMm: Float, val heightMm: Float) {
    A4(210f, 297f),
    A3(297f, 420f),
    LETTER(215.9f, 279.4f)
}

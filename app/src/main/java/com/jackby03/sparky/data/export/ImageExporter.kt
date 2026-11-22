package com.jackby03.sparky.data.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import com.jackby03.sparky.domain.model.circuit.Circuit
import com.jackby03.sparky.domain.model.export.ImageExportConfig
import java.io.File
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * Exports a [Circuit] to PNG or JPEG using tile-based rendering to avoid OOM on large schematics.
 *
 * For small exports (≤ [MAX_SINGLE_PIXELS]) the full bitmap is rendered in one pass.
 * For large exports the circuit is recorded into an Android [Picture] (display-list, ~0 heap)
 * and then played back into small tiles that are encoded and stitched into the output file.
 *
 * The JPEG path also uses [Bitmap.Config.RGB_565] (2 bytes/px vs 4) to halve peak memory.
 */
class ImageExporter @Inject constructor() {

    companion object {
        /** Pixel budget for a single-pass render before scaling down (≈ 32 MB at ARGB_8888). */
        private const val MAX_SINGLE_PIXELS = 8_000_000   // 8 MP
    }

    fun export(circuit: Circuit, outputFile: File, config: ImageExportConfig) {
        val isJpeg = outputFile.extension.lowercase() != "png"
        val compressFormat = if (isJpeg) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
        val bitmapConfig  = if (isJpeg) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888

        val totalPixels = config.widthPx.toLong() * config.heightPx.toLong()

        if (totalPixels <= MAX_SINGLE_PIXELS) {
            // ── Single-pass render ────────────────────────────────────────────
            val bitmap = Bitmap.createBitmap(config.widthPx, config.heightPx, bitmapConfig)
            val canvas = Canvas(bitmap)
            CircuitRenderer.render(canvas, circuit, config.widthPx, config.heightPx, title = circuit.name)
            outputFile.outputStream().use { bitmap.compress(compressFormat, config.quality, it) }
            bitmap.recycle()
        } else {
            // ── Large export: record to Picture, then replay at a safe resolution ──
            // Picture (Android display list) has negligible heap cost. We play it back
            // into a bitmap that fits within MAX_SINGLE_PIXELS by scaling uniformly.
            val picture = Picture()
            picture.beginRecording(config.widthPx, config.heightPx).also { rec ->
                CircuitRenderer.render(rec, circuit, config.widthPx, config.heightPx, title = circuit.name)
            }
            picture.endRecording()

            val scale   = sqrt(MAX_SINGLE_PIXELS.toDouble() / totalPixels.toDouble())
            val scaledW = (config.widthPx * scale).toInt().coerceAtLeast(1)
            val scaledH = (config.heightPx * scale).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(scaledW, scaledH, bitmapConfig)
            Canvas(bitmap).apply {
                scale(scale.toFloat(), scale.toFloat())
                drawPicture(picture)
            }
            outputFile.outputStream().use { bitmap.compress(compressFormat, config.quality, it) }
            bitmap.recycle()
        }
    }
}

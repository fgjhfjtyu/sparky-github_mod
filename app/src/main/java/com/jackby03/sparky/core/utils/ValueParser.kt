package com.jackby03.sparky.core.utils

/**
 * Parses human-friendly component value strings into SI doubles.
 *
 * Supported suffixes (case-insensitive):
 *   T=1e12, G=1e9, M=1e6, k=1e3, m=1e-3, u/µ=1e-6, n=1e-9, p=1e-12, f=1e-15
 *
 * Examples: "1k" → 1000.0, "4.7u" → 4.7e-6, "100m" → 0.1, "5" → 5.0
 */
object ValueParser {

    private val suffixMap = mapOf(
        'T' to 1e12,
        'G' to 1e9,
        'M' to 1e6,
        'k' to 1e3,
        'K' to 1e3,
        'm' to 1e-3,
        'u' to 1e-6,
        'µ' to 1e-6,
        'n' to 1e-9,
        'p' to 1e-12,
        'f' to 1e-15
    )

    /**
     * @return parsed value on success, or null if the string cannot be parsed.
     */
    fun parse(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val lastChar = trimmed.last()
        val multiplier = suffixMap[lastChar]

        return if (multiplier != null) {
            val numericPart = trimmed.dropLast(1)
            numericPart.toDoubleOrNull()?.times(multiplier)
        } else {
            trimmed.toDoubleOrNull()
        }
    }

    /**
     * Formats a double into a compact engineering-notation string with unit suffix.
     */
    fun format(value: Double, unit: String = ""): String {
        return when {
            value == 0.0            -> "0$unit"
            value >= 1e12           -> "${trimTrailingZeros(value / 1e12)}T$unit"
            value >= 1e9            -> "${trimTrailingZeros(value / 1e9)}G$unit"
            value >= 1e6            -> "${trimTrailingZeros(value / 1e6)}M$unit"
            value >= 1e3            -> "${trimTrailingZeros(value / 1e3)}k$unit"
            value >= 1.0            -> "${trimTrailingZeros(value)}$unit"
            value >= 1e-3           -> "${trimTrailingZeros(value * 1e3)}m$unit"
            value >= 1e-6           -> "${trimTrailingZeros(value * 1e6)}µ$unit"
            value >= 1e-9           -> "${trimTrailingZeros(value * 1e9)}n$unit"
            value >= 1e-12          -> "${trimTrailingZeros(value * 1e12)}p$unit"
            else                    -> "${trimTrailingZeros(value * 1e15)}f$unit"
        }
    }

    private fun trimTrailingZeros(value: Double): String {
        val formatted = "%.4f".format(value)
        return formatted.trimEnd('0').trimEnd('.')
    }
}

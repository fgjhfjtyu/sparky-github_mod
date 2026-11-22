package com.jackby03.sparky.engine

import com.jackby03.sparky.core.utils.ValueParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.abs

class ValueParserTest {

    private val tol = 1e-15

    private fun assertParsed(expected: Double, input: String) {
        val result = ValueParser.parse(input)
        checkNotNull(result) { "Expected $expected for input '$input' but got null" }
        check(abs(expected - result) < tol * (1 + abs(expected))) {
            "For '$input': expected $expected but was $result"
        }
    }

    @Test fun `plain number`()       = assertParsed(5.0,    "5")
    @Test fun `kilo suffix`()        = assertParsed(1_000.0, "1k")
    @Test fun `kilo uppercase`()     = assertParsed(1_000.0, "1K")
    @Test fun `milli suffix`()       = assertParsed(1e-3,   "1m")
    @Test fun `micro suffix u`()     = assertParsed(1e-6,   "1u")
    @Test fun `micro suffix mu`()    = assertParsed(4.7e-6, "4.7µ")
    @Test fun `nano suffix`()        = assertParsed(100e-9, "100n")
    @Test fun `pico suffix`()        = assertParsed(47e-12, "47p")
    @Test fun `mega suffix`()        = assertParsed(2.2e6,  "2.2M")
    @Test fun `giga suffix`()        = assertParsed(1e9,    "1G")
    @Test fun `decimal kilo`()       = assertParsed(4_700.0,"4.7k")
    @Test fun `zero`()               = assertParsed(0.0,    "0")
    @Test fun `empty returns null`() = assertNull(ValueParser.parse(""))
    @Test fun `invalid returns null`() = assertNull(ValueParser.parse("abc"))
    @Test fun `whitespace trimmed`() = assertParsed(10.0,   "  10  ")

    @Test
    fun `format kilo`() {
        assertEquals("1k", ValueParser.format(1_000.0))
    }

    @Test
    fun `format micro with unit`() {
        val formatted = ValueParser.format(4.7e-6, "F")
        assertEquals("4.7µF", formatted)
    }

    @Test
    fun `format millivolts`() {
        val formatted = ValueParser.format(0.5e-3, "V")
        assertEquals("0.5mV", formatted)
    }
}

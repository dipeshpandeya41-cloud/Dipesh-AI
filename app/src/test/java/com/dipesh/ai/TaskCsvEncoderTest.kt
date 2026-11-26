package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskCsvEncoderTest {
    @Test
    fun quotesEveryField() {
        assertEquals("\"plain\"", TaskCsvEncoder.encodeCell("plain"))
    }

    @Test
    fun escapesEmbeddedQuotes() {
        assertEquals("\"Say \"\"hello\"\"\"", TaskCsvEncoder.encodeCell("Say \"hello\""))
    }

    @Test
    fun preservesCommasAndLineBreaksInsideQuotedField() {
        assertEquals("\"first, line\nsecond line\"", TaskCsvEncoder.encodeCell("first, line\nsecond line"))
    }

    @Test
    fun encodesEmptyValue() {
        assertEquals("\"\"", TaskCsvEncoder.encodeCell(""))
    }

    @Test
    fun roundTripsSpecialCharactersThroughCsvParser() {
        val values = listOf("comma, value", "Say \"hello\"", "first line\nsecond line", "")
        val csv = values.joinToString(",") { TaskCsvEncoder.encodeCell(it) }

        assertEquals(listOf(values), TaskCsvParser.parseRecords(csv))
    }

    @Test
    fun roundTripsQuotesCommasAndUnicodeTogether() {
        val value = "नेपाल, \"नमस्ते\" 🌏"
        val csv = TaskCsvEncoder.encodeCell(value)

        assertEquals(listOf(listOf(value)), TaskCsvParser.parseRecords(csv))
    }
}

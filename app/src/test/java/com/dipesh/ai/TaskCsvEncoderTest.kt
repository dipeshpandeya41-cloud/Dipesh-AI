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
        assertEquals("\"first, line\\nsecond line\"", TaskCsvEncoder.encodeCell("first, line\nsecond line"))
    }

    @Test
    fun encodesEmptyValue() {
        assertEquals("\"\"", TaskCsvEncoder.encodeCell(""))
    }
}

package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TaskCsvParserTest {
    @Test
    fun parsesCommaInsideQuotedField() {
        assertEquals(
            listOf(listOf("title", "done"), listOf("Call, then email", "false")),
            TaskCsvParser.parseRecords("title,done\n\"Call, then email\",false")
        )
    }

    @Test
    fun parsesEscapedDoubleQuotes() {
        assertEquals(
            listOf(listOf("title"), listOf("Say \"hello\"")),
            TaskCsvParser.parseRecords("title\n\"Say \"\"hello\"\"\"")
        )
    }

    @Test
    fun parsesCrLfRecords() {
        assertEquals(
            listOf(listOf("a", "b"), listOf("1", "2")),
            TaskCsvParser.parseRecords("a,b\r\n1,2\r\n")
        )
    }

    @Test
    fun preservesEmbeddedNewlineInsideQuotedField() {
        assertEquals(
            listOf(listOf("title", "done"), listOf("first line\nsecond line", "false")),
            TaskCsvParser.parseRecords("title,done\n\"first line\nsecond line\",false")
        )
    }

    @Test
    fun ignoresBlankRecords() {
        assertEquals(
            listOf(listOf("a", "b"), listOf("1", "2")),
            TaskCsvParser.parseRecords("a,b\n\n  \n1,2\n")
        )
    }

    @Test
    fun rejectsUnclosedQuotedField() {
        assertThrows(IllegalArgumentException::class.java) {
            TaskCsvParser.parseRecords("title\n\"unfinished")
        }
    }
}

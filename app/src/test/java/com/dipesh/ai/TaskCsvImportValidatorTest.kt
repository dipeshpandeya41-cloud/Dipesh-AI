package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskCsvImportValidatorTest {
    private val valid = listOf("Call, then email", "false", "HIGH", "1800000000000", "42", "true", "1700000000000")

    @Test
    fun parsesValidExportedTaskRow() {
        val result = TaskCsvImportValidator.parse(valid)
        assertEquals("Call, then email", result?.title)
        assertEquals(false, result?.done)
        assertEquals("HIGH", result?.priority)
        assertEquals(1800000000000L, result?.dueAt)
        assertEquals(true, result?.repeatDaily)
        assertEquals(1700000000000L, result?.createdAt)
    }

    @Test
    fun rejectsRowsWithMissingOrExtraColumns() {
        assertNull(TaskCsvImportValidator.parse(valid.dropLast(1)))
        assertNull(TaskCsvImportValidator.parse(valid + "unexpected"))
    }

    @Test
    fun rejectsInvalidBooleanPriorityAndTimestamps() {
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[1] = "yes" }))
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[2] = "URGENT" }))
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[3] = "-1" }))
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[6] = "0" }))
    }

    @Test
    fun rejectsInvalidReminderIds() {
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[4] = "-2" }))
        assertNull(TaskCsvImportValidator.parse(valid.toMutableList().also { it[4] = "2147483648" }))
    }

    @Test
    fun trimsTitleAndAcceptsNumericBooleanValues() {
        val result = TaskCsvImportValidator.parse(valid.toMutableList().also {
            it[0] = "  Reminder title  "
            it[1] = "0"
            it[5] = "1"
        })
        assertEquals("Reminder title", result?.title)
        assertEquals(false, result?.done)
        assertEquals(true, result?.repeatDaily)
    }
}

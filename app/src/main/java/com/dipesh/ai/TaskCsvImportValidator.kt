package com.dipesh.ai

data class ImportedTaskCsvRecord(
    val title: String,
    val done: Boolean,
    val priority: String,
    val dueAt: Long,
    val repeatDaily: Boolean,
    val createdAt: Long
)

object TaskCsvImportValidator {
    private fun parseBoolean(value: String): Boolean? = when (value.trim().lowercase()) {
        "true", "1" -> true
        "false", "0" -> false
        else -> null
    }

    fun parse(fields: List<String>): ImportedTaskCsvRecord? {
        if (fields.size != 7) return null
        val title = fields[0].trim()
        if (title.isBlank()) return null
        val done = parseBoolean(fields[1]) ?: return null
        val priority = fields[2].trim().uppercase()
        if (priority !in setOf("HIGH", "MEDIUM", "LOW")) return null
        val dueAt = fields[3].toLongOrNull()?.takeIf { it >= 0L } ?: return null
        val reminderId = fields[4].toLongOrNull()?.takeIf { it >= 0L } ?: return null
        if (reminderId > Int.MAX_VALUE.toLong()) return null
        val repeatDaily = parseBoolean(fields[5]) ?: return null
        val createdAt = fields[6].toLongOrNull()?.takeIf { it > 0L } ?: return null
        return ImportedTaskCsvRecord(
            title = title.take(180),
            done = done,
            priority = priority,
            dueAt = dueAt,
            repeatDaily = repeatDaily,
            createdAt = createdAt
        )
    }
}

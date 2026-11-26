package com.dipesh.ai

/**
 * Parses RFC-4180-style CSV records, including quoted commas, escaped quotes,
 * CRLF/LF line endings, and embedded newlines inside quoted fields.
 */
internal object TaskCsvParser {
    fun parseRecords(contents: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var fields = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var index = 0

        while (index < contents.length) {
            val ch = contents[index]
            when {
                ch == '"' -> {
                    if (quoted && index + 1 < contents.length && contents[index + 1] == '"') {
                        cell.append('"')
                        index++
                    } else {
                        quoted = !quoted
                    }
                }
                ch == ',' && !quoted -> {
                    fields.add(cell.toString())
                    cell.setLength(0)
                }
                (ch == '\n' || ch == '\r') && !quoted -> {
                    fields.add(cell.toString())
                    cell.setLength(0)
                    if (fields.any { it.isNotBlank() }) records.add(fields)
                    fields = mutableListOf()
                    if (ch == '\r' && index + 1 < contents.length && contents[index + 1] == '\n') index++
                }
                else -> cell.append(ch)
            }
            index++
        }

        if (quoted) throw IllegalArgumentException("Unclosed quoted CSV field")
        fields.add(cell.toString())
        if (fields.any { it.isNotBlank() }) records.add(fields)
        return records
    }
}

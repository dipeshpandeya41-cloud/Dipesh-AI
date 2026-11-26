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
        var afterClosingQuote = false
        var index = 0

        while (index < contents.length) {
            val ch = contents[index]
            when {
                quoted && ch == '"' -> {
                    if (index + 1 < contents.length && contents[index + 1] == '"') {
                        cell.append('"')
                        index++
                    } else {
                        quoted = false
                        afterClosingQuote = true
                    }
                }
                quoted -> cell.append(ch)
                afterClosingQuote && ch == ',' -> {
                    fields.add(cell.toString())
                    cell.setLength(0)
                    afterClosingQuote = false
                }
                afterClosingQuote && (ch == '\n' || ch == '\r') -> {
                    fields.add(cell.toString())
                    cell.setLength(0)
                    if (fields.any { it.isNotBlank() }) records.add(fields)
                    fields = mutableListOf()
                    afterClosingQuote = false
                    if (ch == '\r' && index + 1 < contents.length && contents[index + 1] == '\n') index++
                }
                afterClosingQuote -> throw IllegalArgumentException("Unexpected character after closing quote")
                ch == '"' -> {
                    if (cell.isNotEmpty()) throw IllegalArgumentException("Quote inside an unquoted CSV field")
                    quoted = true
                }
                ch == ',' -> {
                    fields.add(cell.toString())
                    cell.setLength(0)
                }
                ch == '\n' || ch == '\r' -> {
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

package com.dipesh.ai

/** Encodes one CSV field using RFC-4180-style quoting. */
internal object TaskCsvEncoder {
    fun encodeCell(value: String): String {
        val quote = '"'
        return quote + value.replace(quote.toString(), """") + quote
    }
}

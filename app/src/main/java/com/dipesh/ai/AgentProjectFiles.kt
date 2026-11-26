package com.dipesh.ai

/**
 * Extracts generated FILE: path code blocks for a downloadable agent project.
 * Paths are validated before they are ever used as ZIP entry names.
 */
object AgentProjectFiles {
    private val fileBlock = Regex(
        "(?ims)^\\s*(?:#{1,6}\\s*)?FILE\\s*:\\s*([^\\r\\n]+)\\r?\\n\\s*\\x60{3}[a-zA-Z0-9_-]*\\s*\\r?\\n(.*?)\\r?\\n\\s*\\x60{3}"
    )

    fun parse(markdown: String): List<Pair<String, String>> {
        val files = mutableListOf<Pair<String, String>>()
        val seen = mutableSetOf<String>()
        for (match in fileBlock.findAll(markdown)) {
            if (files.size >= 40) break
            val path = match.groupValues[1].trim().replace('\\', '/')
            if (!isSafeRelativePath(path)) continue
            val key = path.lowercase()
            if (!seen.add(key)) continue
            val contents = match.groupValues[2].trimEnd() + "\n"
            if (contents.length > 100_000) continue
            files.add(path to contents)
        }
        if (files.isEmpty()) {
            files.add("README.md" to markdown.take(100_000).trimEnd() + "\n")
        }
        return files
    }

    private fun isSafeRelativePath(path: String): Boolean {
        if (path.isBlank() || path.length > 180 || path.startsWith("/") || path.startsWith("\\\\")) return false
        if (path.contains(':') || path.any { it.isISOControl() }) return false
        val segments = path.split('/')
        if (segments.any { it.isBlank() || it == "." || it == ".." }) return false
        return segments.all { it.matches(Regex("[A-Za-z0-9_.-]+")) }
    }
}

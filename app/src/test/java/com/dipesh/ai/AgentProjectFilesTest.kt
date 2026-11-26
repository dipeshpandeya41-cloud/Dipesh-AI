package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentProjectFilesTest {
    private fun block(path: String, language: String, body: String): String {
        val fence = 96.toChar().toString().repeat(3)
        return "FILE: $path\n$fence$language\n$body\n$fence"
    }

    @Test
    fun extractsMultipleFilesAndPreservesRelativePaths() {
        val markdown = listOf(
            block("src/main.py", "python", "print(\"hello\")"),
            block("requirements.txt", "text", "requests")
        ).joinToString("\n")

        val files = AgentProjectFiles.parse(markdown)
        assertEquals(listOf("src/main.py", "requirements.txt"), files.map { it.first })
        assertTrue(files.first().second.contains("print"))
    }

    @Test
    fun rejectsTraversalAbsoluteAndInvalidPaths() {
        val markdown = listOf(
            block("../escape.txt", "text", "bad"),
            block("/absolute.txt", "text", "bad"),
            block("C:/drive.txt", "text", "bad"),
            block("src/../../escape.py", "python", "bad"),
            block("safe/main.py", "python", "print(\"safe\")")
        ).joinToString("\n")

        val files = AgentProjectFiles.parse(markdown)
        assertEquals(listOf("safe/main.py"), files.map { it.first })
    }

    @Test
    fun ignoresDuplicatePathsCaseInsensitively() {
        val markdown = listOf(
            block("app.py", "python", "first"),
            block("APP.py", "python", "second")
        ).joinToString("\n")

        assertEquals(1, AgentProjectFiles.parse(markdown).size)
        assertTrue(AgentProjectFiles.parse(markdown).single().second.contains("first"))
    }

    @Test
    fun fallsBackToMarkdownWhenNoFileBlocksExist() {
        val files = AgentProjectFiles.parse("# Agent plan")
        assertEquals("README.md", files.single().first)
        assertTrue(files.single().second.contains("Agent plan"))
    }

    @Test
    fun limitsExportedFileCount() {
        val markdown = (1..45).joinToString("\n") { index ->
            block("file$index.txt", "text", index.toString())
        }
        assertEquals(40, AgentProjectFiles.parse(markdown).size)
    }
}

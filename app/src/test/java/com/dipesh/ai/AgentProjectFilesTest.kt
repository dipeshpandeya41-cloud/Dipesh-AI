package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentProjectFilesTest {
    @Test
    fun extractsMultipleFilesAndPreservesRelativePaths() {
        val markdown = """
            ## Project files
            FILE: src/main.py
            ```python
            print("hello")
            ```
            FILE: requirements.txt
            ```text
            requests
            ```
        """.trimIndent()

        val files = AgentProjectFiles.parse(markdown)
        assertEquals(listOf("src/main.py", "requirements.txt"), files.map { it.first })
        assertTrue(files.first().second.contains("print"))
    }

    @Test
    fun rejectsTraversalAbsoluteAndInvalidPaths() {
        val markdown = """
            FILE: ../escape.txt
            ```text
            bad
            ```
            FILE: /absolute.txt
            ```text
            bad
            ```
            FILE: C:/drive.txt
            ```text
            bad
            ```
            FILE: src/../../escape.py
            ```python
            bad
            ```
            FILE: safe/main.py
            ```python
            print("safe")
            ```
        """.trimIndent()

        val files = AgentProjectFiles.parse(markdown)
        assertEquals(listOf("safe/main.py"), files.map { it.first })
    }

    @Test
    fun ignoresDuplicatePathsCaseInsensitively() {
        val markdown = """
            FILE: app.py
            ```python
            first
            ```
            FILE: APP.py
            ```python
            second
            ```
        """.trimIndent()

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
        val markdown = (1..45).joinToString("\\n") { index ->
            "FILE: file$index.txt\\n```text\\n$index\\n```"
        }
        assertEquals(40, AgentProjectFiles.parse(markdown).size)
    }
}

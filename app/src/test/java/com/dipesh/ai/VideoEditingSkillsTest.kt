package com.dipesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoEditingSkillsTest {
    @Test
    fun exposesExactlyThreeVideoEditingSkills() {
        assertEquals(3, VideoEditingSkills.all.size)
        assertEquals(listOf("Remotion", "FFmpeg", "MoviePy"), VideoEditingSkills.all.map { it.name })
        assertTrue(VideoEditingSkills.all.all { it.description.isNotBlank() })
    }
}

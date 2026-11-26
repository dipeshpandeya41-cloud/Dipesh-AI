package com.dipesh.ai

/**
 * Exactly three video-editing skill profiles used by the Video Studio project generator.
 * These describe project-generation targets; they do not bundle or execute the tools.
 */
object VideoEditingSkills {
    data class Skill(val name: String, val description: String)

    val all = listOf(
        Skill(
            "Remotion",
            "React + TypeScript programmatic video compositions, animated titles, scenes, and render scripts. Check Remotion licensing for your usage."
        ),
        Skill(
            "FFmpeg",
            "Command-line video and audio editing: trim, join, resize, subtitles, filters, audio mixing, and encoding."
        ),
        Skill(
            "MoviePy",
            "Python-based editing workflows for clips, text overlays, transitions, audio, and automated video assembly."
        )
    )
}

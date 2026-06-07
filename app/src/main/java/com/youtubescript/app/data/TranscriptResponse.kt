package com.youtubescript.app.data

data class TranscriptSegment(
    val text: String,
    val start: Double,
    val duration: Double
)

data class TranscriptResult(
    val segments: List<TranscriptSegment>,
    val title: String?,
    val error: String?
) {
    val fullText: String
        get() = segments.joinToString(" ") { it.text.trim() }

    val isSuccess: Boolean
        get() = error == null
}

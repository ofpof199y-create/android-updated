package com.youtubescript.app.data

import android.net.Uri
import android.speech.SpeechRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TranscriptRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getTranscript(videoUrl: String): TranscriptResult = withContext(Dispatchers.IO) {
        val videoId = extractVideoId(videoUrl)
        if (videoId == null) {
            return@withContext TranscriptResult(
                segments = emptyList(), title = null,
                error = "رابط يوتيوب غير صالح"
            )
        }

        try {
            val title = fetchVideoTitle(videoId)
            val segments = fetchTranscript(videoId)
            TranscriptResult(segments = segments, title = title, error = null)
        } catch (e: Exception) {
            TranscriptResult(
                segments = emptyList(), title = null,
                error = when {
                    e.message?.contains("403") == true -> "الفيديو لا يحتوي على نص مكتوب متاح"
                    e.message?.contains("404") == true -> "لم يتم العثور على الفيديو"
                    else -> "حدث خطأ: ${e.message ?: "غير معروف"}"
                }
            )
        }
    }

    suspend fun getLocalVideoTranscript(uri: Uri): TranscriptResult = withContext(Dispatchers.IO) {
        // Local video transcript via YouTube's timedtext won't work,
        // so we return a message guiding the user
        TranscriptResult(
            segments = emptyList(),
            title = "فيديو محلي",
            error = "استخراج النص من الفيديوهات المحلية غير متاح حالياً. يرجى رفع الفيديو على يوتيوب أولاً."
        )
    }

    private fun extractVideoId(url: String): String? {
        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/embed/|youtube\.com/shorts/)([a-zA-Z0-9_-]{11})"""),
            Regex("""^([a-zA-Z0-9_-]{11})$""")
        )
        for (pattern in patterns) {
            pattern.find(url)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }

    private fun fetchVideoTitle(videoId: String): String? {
        return try {
            val request = Request.Builder()
                .url("https://www.youtube.com/watch?v=$videoId")
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
                .addHeader("Accept-Language", "en-US,en;q=0.9")
                .build()
            val html = client.newCall(request).execute().body?.string() ?: return null
            val titleMatch = Regex("""<title>(.+?)<\/title>""").find(html)
            titleMatch?.groupValues?.get(1)?.replace(" - YouTube", "")?.trim()
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchTranscript(videoId: String): List<TranscriptSegment> {
        val pageRequest = Request.Builder()
            .url("https://www.youtube.com/watch?v=$videoId")
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .build()

        val html = client.newCall(pageRequest).execute().body?.string()
            ?: throw Exception("فشل تحميل الصفحة")

        val captionMatch = Regex(""""captionTracks":\[{"baseUrl":"([^"]+)"""").find(html)
            ?: throw Exception("404")

        val captionUrl = captionMatch.groupValues[1].replace("\\u0026", "&")

        val capRequest = Request.Builder()
            .url(captionUrl)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36")
            .build()

        val xml = client.newCall(capRequest).execute().body?.string()
            ?: throw Exception("استجابة فارغة")

        val segments = mutableListOf<TranscriptSegment>()
        val regex = Regex("""<text start="([^"]+)" dur="([^"]+)"[^>]*>([\s\S]*?)</text>""")

        regex.findAll(xml).forEach { match ->
            val text = match.groupValues[3]
                .replace(Regex("<[^>]+>"), "")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()
            if (text.isNotEmpty()) {
                segments.add(
                    TranscriptSegment(
                        text = text,
                        start = match.groupValues[1].toDouble(),
                        duration = match.groupValues[2].toDouble()
                    )
                )
            }
        }

        if (segments.isEmpty()) throw Exception("لا يوجد نص في هذا الفيديو")
        return segments
    }
}

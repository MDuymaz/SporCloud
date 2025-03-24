package com.SporCloud

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.Qualities
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class DiskYandexComTr : ExtractorApi() {
    override val name: String             = "DiskYandexComTr"
    override val mainUrl: String          = "https://disk.yandex.com.tr"
    override val requiresReferer: Boolean = false

    // Regex pattern to extract master-playlist.m3u8 URLs
    private val masterPlaylistRegex = Pattern.compile("https?://[^\\s\"]*?master-playlist\\.m3u8")

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        // Create a request with headers
        val request = app.get(
            url     = url,
            referer = "https://disk.yandex.com.tr/",
            headers = mapOf(
                "X-Requested-With" to "XMLHttpRequest"
            )
        )

        // Check if the request was successful
        if (!request.isSuccessful) {
            throw Exception("Failed to fetch URL: ${request.code}")
        }

        // Veriyi InputStream olarak oku (belleğe yüklemek yerine akış halinde işler)
        val inputStream = request.body?.byteStream() ?: throw Exception("Failed to get response body")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }

        reader.close()
        val htmlContent = stringBuilder.toString()

        // Extract the master-playlist.m3u8 URL using regex
        val matcher = masterPlaylistRegex.matcher(htmlContent)
        if (matcher.find()) {
            val masterPlaylistUrl = matcher.group()

            // Create an ExtractorLink for the master-playlist.m3u8 URL
            val extractorLink = ExtractorLink(
                source  = "Yandex Disk",
                name    = "Yandex Disk",
                url     = masterPlaylistUrl,
                referer = referer ?: "",
                quality = Qualities.Unknown.value,
                type    = ExtractorLinkType.M3U8
            )

            // Invoke the callback with the ExtractorLink
            callback.invoke(extractorLink)
        } else {
            throw Exception("No master-playlist.m3u8 URL found in the response")
        }
    }
}

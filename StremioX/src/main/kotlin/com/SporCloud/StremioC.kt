package com.SporCloud

import com.fasterxml.jackson.annotation.JsonProperty
import com.SporCloud.SubsExtractors.invokeOpenSubs
import com.SporCloud.SubsExtractors.invokeWatchsomuch
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbId
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson

class StremioC : MainAPI() {
    override var mainUrl = "https://stremio.github.io/stremio-static-addon-example"
    override var name = "StremioC"
    override val supportedTypes = setOf(TvType.Others)
    override val hasMainPage = true

    companion object {
        private const val cinemataUrl = "https://v3-cinemeta.strem.io"
        private const val TRACKER_LIST_URL = "https://raw.githubusercontent.com/ngosang/trackerslist/master/trackers_best.txt"
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        mainUrl = mainUrl.fixUrl()
        val res = app.get("${mainUrl}/manifest.json").parsedSafe<Manifest>()
        val lists = res?.catalogs?.mapNotNull { catalog ->
            catalog.toHomePageList(this).takeIf { it.list.isNotEmpty() }
        } ?: emptyList()
        return HomePageResponse(lists, false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        mainUrl = mainUrl.fixUrl()
        val res = app.get("${mainUrl}/manifest.json").parsedSafe<Manifest>()
        return res?.catalogs?.flatMap { catalog -> catalog.search(query, this) }?.distinct() ?: emptyList()
    }

    override suspend fun load(url: String): LoadResponse {
        val res = parseJson<CatalogEntry>(url)
        mainUrl = if ((res.type == "movie" || res.type == "series") && isImdbOrTmdb(res.id)) cinemataUrl else mainUrl
        val json = app.get("${mainUrl}/meta/${res.type}/${res.id}.json").parsedSafe<CatalogResponse>()?.meta
            ?: throw RuntimeException(url)
        return json.toLoadResponse(this, res.id)
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val loadData = parseJson<LoadData>(data)
        val request = app.get("${mainUrl}/stream/${loadData.type}/${loadData.id}.json", timeout = 10000)
        if (request.isSuccessful) {
            request.parsedSafe<StreamsResponse>()?.streams?.forEach { stream ->
                stream.runCallback(subtitleCallback, callback)
            }
        } else {
            listOf(
                { invokeStremioX(loadData.type, loadData.id, subtitleCallback, callback) },
                { invokeWatchsomuch(loadData.imdbId, loadData.season, loadData.episode, subtitleCallback) },
                { invokeOpenSubs(loadData.imdbId, loadData.season, loadData.episode, subtitleCallback) }
            ).forEach { it() }
        }
        return true
    }

    private suspend fun invokeStremioX(
        type: String?,
        id: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val sites = AcraApplication.getKey<Array<CustomSite>>(USER_PROVIDER_API)?.toList().orEmpty()
        sites.filter { it.parentJavaClass == "StremioX" }.forEach { site ->
            app.get("${site.url.fixUrl()}/stream/${type}/${id}.json", timeout = 10000)
                .parsedSafe<StreamsResponse>()?.streams?.forEach { stream ->
                    stream.runCallback(subtitleCallback, callback)
                }
        }
    }

    private fun isImdbOrTmdb(url: String?): Boolean {
        return imdbUrlToIdNullable(url) != null || url?.startsWith("tmdb:") == true
    }
}

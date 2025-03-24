package com.SporCloud

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class HiAnimeProviderPlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(AnimeKai())
        registerExtractorAPI(MegaUp())
    }
}

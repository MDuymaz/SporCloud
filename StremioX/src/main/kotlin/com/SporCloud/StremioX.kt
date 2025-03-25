package com.SporCloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.SporCloud.SubsExtractors.invokeOpenSubs;
import com.SporCloud.SubsExtractors.invokeWatchsomuch;
import com.lagradost.cloudstream3.*;
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbId;
import com.lagradost.cloudstream3.LoadResponse.Companion.addTMDbId;
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer;
import com.lagradost.cloudstream3.utils.*;
import com.lagradost.cloudstream3.utils.AppUtils.parseJson;
import com.lagradost.cloudstream3.utils.AppUtils.toJson;
import java.util.ArrayList;
import kotlin.math.roundToInt;
import com.lagradost.cloudstream3.metaproviders.TmdbProvider;

class StremioX : TmdbProvider() {
    override var mainUrl = "https://torrentio.strem.fun";
    override var name = "StremioX";
    override val hasMainPage = true;
    override val hasQuickSearch = true;
    override val supportedTypes = setOf(TvType.Others);

    companion object {
        private const val tmdbAPI = "https://api.themoviedb.org/3";
        private const val apiKey = BuildConfig.TMDB_API;

        fun getType(t: String?): TvType {
            return when (t) {
                "movie" -> TvType.Movie;
                else -> TvType.TvSeries;
            }
        }

        fun getStatus(t: String?): ShowStatus {
            return when (t) {
                "Returning Series" -> ShowStatus.Ongoing;
                else -> ShowStatus.Completed;
            }
        }
    }

    override val mainPage = mainPageOf(
        "$tmdbAPI/trending/all/day?api_key=$apiKey&region=US" to "Trending",
        "$tmdbAPI/movie/popular?api_key=$apiKey&region=US" to "Popular Movies",
        "$tmdbAPI/tv/popular?api_key=$apiKey&region=US&with_original_language=en" to "Popular TV Shows",
    );

    override suspend fun search(query: String): List<SearchResponse>? {
        return app.get(
            "$tmdbAPI/search/multi?api_key=$apiKey&language=en-US&query=$query&page=1&include_adult=false"
        ).parsedSafe<Results>()?.results?.mapNotNull { media ->
            media.toSearchResponse();
        };
    }

    override suspend fun load(url: String): LoadResponse? {
        val data = parseJson<Data>(url);
        val type = getType(data.type);
        val resUrl = "$tmdbAPI/${data.type}/${data.id}?api_key=$apiKey&append_to_response=videos";
        val res = app.get(resUrl).parsedSafe<MediaDetail>() ?: return null;

        val title = res.title ?: res.name ?: return null;
        val poster = res.posterPath;
        val trailer = res.videos?.results?.firstOrNull()?.key?.let { "https://www.youtube.com/watch?v=$it" };

        return newMovieLoadResponse(title, url, type, null) {
            this.posterUrl = poster;
            this.backgroundPosterUrl = res.backdropPath;
            this.year = res.releaseDate?.split("-")?.first()?.toIntOrNull();
            addTrailer(trailer);
            addTMDbId(data.id.toString());
            addImdbId(res.externalIds?.imdbId);
        };
    }
}

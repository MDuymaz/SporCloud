version = 71

cloudstream {
    authors     = listOf("SporCloud")
    language    = "tr"
    description = "RecTv APK"

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("Movie", "Live", "TvSeries")
    iconUrl = "https://rectvapk.cc/wp-content/uploads/2023/02/Rec-TV.webp"
}

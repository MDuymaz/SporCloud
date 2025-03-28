version = 22

cloudstream {
    authors     = listOf("SporCloud")
    language    = "tr"
    description = "Live TV."

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
    **/
    status  = 1 // will be 3 if unspecified
    tvTypes = listOf("Live")
    iconUrl = "https://e7.pngegg.com/pngimages/955/749/png-clipart-ape-monkey-cartoon-sad-monkey-face-mammal-vertebrate.png"
}

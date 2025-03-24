import org.jetbrains.kotlin.konan.properties.Properties

// use an integer for version numbers
version = 14

android {
    defaultConfig {
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "TMDB_API", "\"${properties.getProperty("TMDB_API")}\"")
    }
}

cloudstream {
    language = "en"
    // All of these properties are optional, you can safely remove them

     description = "[!] Requires Setup \n- StremioX allows you to use stream addons \n- StremioC allows you to use catalog addons"
     authors = listOf("SporCloud")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "TvSeries",
        "Movie",
    )

    iconUrl = "https://is1-ssl.mzstatic.com/image/thumb/Purple123/v4/1c/ef/01/1cef01c8-00be-1f19-2f97-10dce27b71c3/AppIcon-1x_U007emarketing-0-7-0-85-220.png/1200x630wa.png"
}

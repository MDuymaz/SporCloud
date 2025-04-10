import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0") // Android Gradle Plugin sürümünü güncelledik
        classpath("com.github.recloudstream:gradle:master-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) = extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "https://github.com/MDuymaz/SporCloud")
        authors = listOf("SporCloud")
    }

    android {
        namespace = "com.SporCloud"

        defaultConfig {
            minSdk = 21
            compileSdkVersion(35)
            targetSdk = 35
            buildConfig = true // buildConfig özelliği buraya eklendi
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
                freeCompilerArgs.addAll(
                    listOf(
                        "-Xno-call-assertions",
                        "-Xno-param-assertions",
                        "-Xno-receiver-assertions"
                    )
                )
            }
        }
    }

    dependencies {
        val cloudstream by configurations
        val implementation by configurations

        cloudstream("com.lagradost:cloudstream3:pre-release")

        implementation(kotlin("stdlib"))
        implementation("com.github.Blatzar:NiceHttp:0.4.11")
        implementation("org.jsoup:jsoup:1.18.3")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
        implementation("io.karn:khttp-android:0.1.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation("org.mozilla:rhino:1.8.0")
        implementation("com.google.code.gson:gson:2.8.9")
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

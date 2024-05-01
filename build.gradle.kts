import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.buildKonfig) apply false
    alias(libs.plugins.graalVM) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.dotenvGradle)
}

detekt {
    toolVersion = libs.versions.detekt.get()

    config.setFrom("config/detekt/detekt.yml")

    source.setFrom(
        "composeApp/src/androidMain/kotlin",
        "composeApp/src/commonMain/kotlin",
        "composeApp/src/desktopMain/kotlin",
        "composeApp/src/iosMain/kotlin",
        "composeApp/src/wasmJsMain/kotlin",

        "server/src/main/kotlin",

        "shared/src/commonMain/kotlin",
    )

    autoCorrect = true
}

tasks.withType(Detekt::class) {
    reports {
        html.required.set(true)
        md.required.set(false)
        xml.required.set(false)
        sarif.required.set(false)
        txt.required.set(false)
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
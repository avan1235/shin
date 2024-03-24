plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.graalVM)
    application
}

group = "in.procyk.shin"
version = "1.0.0"
application {
    mainClass.set("in.procyk.shin.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.cbor)

    implementation(libs.koin.ktor)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgres)
    implementation(libs.dotenv)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}

graalvmNative {
    binaries {
        named("main") {
            resources.autodetect()
            fallback.set(false)
            verbose.set(true)

            buildArgs(
                "--initialize-at-build-time=ch.qos.logback",
                "--initialize-at-build-time=io.ktor,kotlin",
                "--initialize-at-build-time=org.slf4j.LoggerFactory",

                "--initialize-at-build-time=kotlinx.serialization.modules.SerializersModuleKt",
                "--initialize-at-build-time=kotlinx.serialization.cbor.Cbor\$Default",
                "--initialize-at-build-time=kotlinx.serialization.cbor.Cbor",
                "--initialize-at-build-time=kotlinx.serialization.cbor.CborImpl",

                "-H:+InstallExitHandlers",
                "-H:+ReportUnsupportedElementsAtRuntime",
                "-H:+ReportExceptionStackTraces",
            )

            imageName.set("server")
        }
    }
}

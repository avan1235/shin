import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem as currentOS
import java.lang.System.getenv

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.buildKonfig)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                val corsPort = env.CORS_PORT.value.toInt()
                devServer = devServer?.copy(port = corsPort) ?: KotlinWebpackConfig.DevServer(port = corsPort)
            }
        }
        binaries.executable()
        applyBinaryen()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop")

    if (currentOS().isMacOsX) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                export(libs.decompose)
                export(libs.essenty.lifecycle)
                export(libs.essenty.statekeeper)

                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.apply {
                optIn("androidx.compose.material3.ExperimentalMaterial3Api")
                optIn("androidx.compose.ui.ExperimentalComposeUiApi")
                optIn("com.arkivanov.decompose.ExperimentalDecomposeApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
            }
        }

        val desktopMain by getting
        val wasmJsMain by getting

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(projects.shared)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.cbor)

            implementation(libs.alexzhirkevich.qrose)
            implementation(libs.procyk.compose.calendar)

            implementation(libs.decompose)
            implementation(libs.decompose.extensionsComposeJetbrains)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)

            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java)
        }
        if (currentOS().isMacOsX) {
            iosMain.dependencies {
                api(libs.decompose)
                api(libs.essenty.lifecycle)
                api(libs.essenty.statekeeper)

                implementation(libs.ktor.client.darwin)
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

android {
    namespace = "in.procyk.shin"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "in.procyk.shin"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = getAndBumpVersionCode()
        versionName = getenv()["VERSION"] ?: "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "compose-android.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

compose {
    desktop.application {
        mainClass = "MainKt"
        version = env.VERSION.value

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "shin"
            modules("java.net.http")

            windows {
                menu = false
                upgradeUuid = "1a273513-558a-4322-9833-ff3a4c3c1173"
            }

            macOS {
                bundleID = "in.procyk.shin"
                appStore = false
                signing {
                    sign.set(false)
                }
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
            isEnabled = true
            optimize = true
            obfuscate = false
        }
    }
    experimental.web.application {}
}

buildkonfig {
    packageName = "in.procyk.shin"
    objectName = "ComposeAppConfig"

    defaultConfigs {
        buildConfigField(Type.STRING, "CLIENT_HOST", env.CLIENT_HOST.value)
        buildConfigField(Type.STRING, "CLIENT_PROTOCOL", env.CLIENT_PROTOCOL.value)
    }
}

fun getAndBumpVersionCode(): Int {
    val code = libs.versions.versionCode.get().toInt()
    val bump = getenv()["BUMP_FILE_VERSION_CODE"]?.toBooleanStrictOrNull() ?: false
    if (!bump) return code

    val file = File("gradle/libs.versions.toml")
    val updatedFile = file.readLines().map { line ->
        if (!line.startsWith("versionCode")) return@map line

        val currentVersionCode = line
            .dropWhile { it != '"' }
            .removePrefix("\"")
            .takeWhile { it != '"' }
            .toInt()
        if (currentVersionCode != code) throw IllegalStateException("Two different version codes: $code vs $currentVersionCode")

        """versionCode = "${currentVersionCode + 1}""""
    }.joinToString(separator = "\n")
    file.writeText(updatedFile)
    return code
}

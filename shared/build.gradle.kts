import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary) // It is a Library, not an App
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)  // Needed for Room
    alias(libs.plugins.room) // Needed for Room
    alias(libs.plugins.buildkonfig) // Storing secrets in local.properties
}

// --- SECURELY READ THE KEY ---
val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localProperties.load(localFile.inputStream())
}

// Try to get from local.properties, fallback to System Environment (good for CI/CD)
val webClientId: String = localProperties.getProperty("WEB_CLIENT_ID")
    ?: System.getenv("WEB_CLIENT_ID")
    ?: "MISSING_API_KEY"

// --- GENERATE THE CONFIG ---
buildkonfig {
    packageName = "com.eeseka.lynk"
    objectName = "AppConfig"
    exposeObjectWithName = "AppConfig"

    defaultConfigs {
        buildConfigField(STRING, "WEB_CLIENT_ID", webClientId)
    }
    // Debug builds
    defaultConfigs("debug") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "true")
    }

    // Release builds
    defaultConfigs("release") {
        buildConfigField(BOOLEAN, "IS_DEBUG", "false")
    }
}

kotlin {
    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        namespace = "com.eeseka.lynk.shared"
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            // --- UI Essentials ---
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)

            // -- Permission handling ---
            implementation(libs.moko.permissions)
            implementation(libs.moko.permissions.compose)
            implementation(libs.moko.permissions.notifications)

            // -- Logging --
            implementation(libs.touchlab.kermit)

            // --- Architecture ---
            implementation(libs.bundles.koin.common)
            implementation(libs.bundles.ktor.common)

            // --- Capabilities (ShelfLife Stack) ---
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // -- Icons --
            implementation(libs.icons.lucide.cmp)
            implementation(libs.jetbrains.compose.material.icons.extended)

            // --- Data & State ---
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.datetime)
            implementation(libs.touchlab.kermit)

            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.material3.adaptive)

            // --- Database (Room) ---
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // Cupertino
            implementation(libs.cupertino)
            implementation(libs.cupertino.icons.extended)
            implementation(libs.cupertino.native)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

// Room Schema Setup
room {
    schemaDirectory("$projectDir/schemas")
}

// KSP Configuration for Room (Must be in the module that uses Room)
dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}
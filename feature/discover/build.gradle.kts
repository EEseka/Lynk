plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary) // It is a Library, not an App
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        namespace = "com.eeseka.lynk.discover"
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Discover"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.maplibre.native.ffi.runtime.opengl)
        }

        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.bundles.koin.common)

            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.icons.lucide.cmp)

            implementation(libs.maplibre.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.kotlinx.collections.immutable)

            implementation(projects.shared)
            implementation(projects.feature.createHangout)
        }

        commonTest.dependencies {
            implementation(projects.testing)
        }
    }
}
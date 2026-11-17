plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        namespace = "com.eeseka.lynk.shared"
        androidResources { enable = true }

        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "com.eeseka.lynk.Shared")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.bundles.koin.common)

            // Needed for GoogleAuthProvider app initialization
            implementation(libs.auth.google.kmp)
            
            implementation(projects.core)
            implementation(projects.feature.onboarding)
            implementation(projects.feature.auth)
            implementation(projects.feature.profileSetup)
            implementation(projects.feature.mainShell)
            implementation(projects.feature.discover)
            implementation(projects.feature.createHangout)
            implementation(projects.feature.hangouts)
            implementation(projects.feature.profile)
            implementation(projects.feature.notifications)
            implementation(projects.feature.assistant)
        }
        commonTest.dependencies {
            implementation(projects.testing)
        }
    }
}
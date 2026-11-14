plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary) // It is a Library, not an App
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        namespace = "com.eeseka.lynk.main_shell"
        androidResources { enable = true }

        withHostTest {}
    }

    iosArm64()
    iosSimulatorArm64()

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

            implementation(libs.bundles.koin.common)

            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.icons.lucide.cmp)

            implementation(libs.adaptive.ui)

            implementation(projects.core)
            implementation(projects.feature.discover)
            implementation(projects.feature.hangouts)
            implementation(projects.feature.profile)
        }

        commonTest.dependencies {
            implementation(projects.testing)
        }
    }
}
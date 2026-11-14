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
        namespace = "com.eeseka.lynk.create_hangout"
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

            implementation(libs.icons.lucide.cmp)

            implementation(libs.bundles.koin.common)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.collections.immutable)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(projects.core)
        }

        commonTest.dependencies {
            implementation(projects.testing)
        }
    }
}
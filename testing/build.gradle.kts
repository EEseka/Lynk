plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary) // It is a Library, not an App
}

kotlin {
    android {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        namespace = "com.eeseka.lynk.testing"
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // api, not implementation: every test file imports these, so they reach each module that uses :testing
            api(libs.kotlin.test)
            api(libs.turbine)
            api(libs.assertk)
            api(libs.kotlinx.coroutines.test)

            implementation(libs.foundation)

            implementation(projects.shared)
        }

        androidMain.dependencies {
            api(libs.kotlin.testJunit)
        }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

composeCompiler {
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("compose-stability.conf")
}

android {
    namespace = "com.havos.lubricerp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.havos.lubricerp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("demo") {
            dimension = "environment"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            buildConfigField("boolean", "USE_MOCK_ENGINE", "true")
            buildConfigField("String", "ENVIRONMENT", "\"TEST\"")
            buildConfigField("String", "BASE_URL", "\"http://havostech-001-site2.atempurl.com/\"")
        }
        create("stage") {
            dimension = "environment"
            applicationIdSuffix = ".stage"
            versionNameSuffix = "-stage"
            buildConfigField("boolean", "USE_MOCK_ENGINE", "false")
            buildConfigField("String", "ENVIRONMENT", "\"STAGE\"")
            buildConfigField("String", "BASE_URL", "\"http://havostech-001-site2.atempurl.com/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("boolean", "USE_MOCK_ENGINE", "false")
            buildConfigField("String", "ENVIRONMENT", "\"PRODUCTION\"")
            buildConfigField("String", "BASE_URL", "\"http://havostech-001-site2.atempurl.com/\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("key.jks")
            storePassword = "goalErp@2026"
            keyAlias = "GoalErp2026"
            keyPassword = "goalErp@2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (rootProject.file("key.jks").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":feature_reports:data"))
    implementation(project(":feature_reports:domain"))
    implementation(project(":feature_reports:presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation(libs.androidx.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

import com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig
import java.text.SimpleDateFormat


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "io.homeassistant.companion.android"

    compileSdk = libs.versions.androidSdk.compile.get().toInt()

    ndkVersion = "21.3.6528147"

    useLibrary("android.car")

    defaultConfig {
        applicationId = "io.homeassistant.companion.android"
        minSdk = libs.versions.androidSdk.min.get().toInt()
        targetSdk = libs.versions.androidSdk.target.get().toInt()

        versionName = getVersionName()
        versionCode = getVersionCode()

        manifestPlaceholders["sentryRelease"] = "$applicationId@$versionName"
        manifestPlaceholders["sentryDsn"] = System.getenv("SENTRY_DSN") ?: ""

        bundle {
            language {
                enableSplit = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
    }

    compileOptions {
        sourceCompatibility(libs.versions.javaVersion.get())
        targetCompatibility(libs.versions.javaVersion.get())
    }

    firebaseAppDistribution {
        serviceCredentialsFile = "firebaseAppDistributionServiceCredentialsFile.json"
        releaseNotesFile = "./app/build/outputs/changelogBeta"
        groups = "continuous-deployment"
    }

    val NESTOR_KEYSTORE_PASSWORD = System.getenv("NESTOR_KEYSTORE_PASSWORD")
    val NESTOR_KEYSTORE_ALIAS = System.getenv("NESTOR_KEYSTORE_ALIAS")
   // val PGY_API_KEY = System.getenv("PGY_API_KEY")

    signingConfigs {
        create("release") {
            storeFile = file("../nestor.keystore")
            storePassword = NESTOR_KEYSTORE_PASSWORD
            keyAlias = NESTOR_KEYSTORE_ALIAS
            keyPassword = NESTOR_KEYSTORE_PASSWORD
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        named("debug").configure {
            signingConfig = signingConfigs.getByName("release")
            //manifestPlaceholders["pgy_api_key"] = PGY_API_KEY
        }
        named("release").configure {
            isDebuggable = false
            isJniDebuggable = false
            signingConfig = signingConfigs.getByName("release")
           // manifestPlaceholders["pgy_api_key"] = PGY_API_KEY
        }
    }
    flavorDimensions.add("version")
    productFlavors {
//        create("minimal") {
//            applicationIdSuffix = ".minimal"
//            versionNameSuffix = "-minimal"
//        }
        create("full") {
            applicationIdSuffix = ""
            versionNameSuffix = "-full"
        }

        // Generate a list of application ids into BuildConfig
        val values = productFlavors.joinToString {
            "\"${it.applicationId ?: defaultConfig.applicationId}${it.applicationIdSuffix}\""
        }

        defaultConfig.buildConfigField("String[]", "APPLICATION_IDS", "{$values}")
    }

//    playConfigs {
////        register("minimal") {
////            enabled.set(false)
////        }
//        register("full") {
//            enabled.set(false)
//        }
//    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":common"))

    implementation(libs.blurView)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    "fullImplementation"(libs.kotlinx.coroutines.play.services)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.fragment.ktx)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.okhttp)
    implementation(libs.picasso)

    "fullImplementation"(libs.play.services.location)
    "fullImplementation"(libs.play.services.home)
    "fullImplementation"(libs.play.services.threadnetwork)
    "fullImplementation"(platform(libs.firebase.bom))
    "fullImplementation"(libs.firebase.messaging)
    "fullImplementation"(libs.sentry.android)
    "fullImplementation"(libs.play.services.wearable)
    "fullImplementation"(libs.wear.remote.interactions)
    "fullImplementation"(libs.crashlytics)
    "fullImplementation"(libs.analytics)
    "fullImplementation"(libs.amap)

    implementation(libs.biometric)
    implementation(libs.webkit)

    implementation(libs.bundles.media3)
    "fullImplementation"(libs.media3.datasource.cronet)
//    "minimalImplementation"(libs.media3.datasource.cronet) {
//        exclude(group = "com.google.android.gms", module = "play-services-cronet")
//    }
//    "minimalImplementation"(libs.cronet.embedded)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.compiler)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiTooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.iconics.core)
    implementation(libs.iconics.compose)
    implementation(libs.community.material.typeface)

    implementation(libs.bundles.paging)

    implementation(libs.reorderable)
    implementation(libs.changeLog)

    implementation(libs.car.core)
    "fullImplementation"(libs.car.projected)
}

// Disable to fix memory leak and be compatible with the configuration cache.
configure<GoogleServicesPluginConfig> {
    disableVersionCheck = true
}

fun getVersionCode(): Int {
    val time = System.currentTimeMillis()
    return (time / 1000).toInt()
}

fun getVersionName(): String {
    return "v" + SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
}

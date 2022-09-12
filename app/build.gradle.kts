plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Versions.Sdk
    buildToolsVersion = Versions.BuildTools

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    defaultConfig {
        applicationId = "org.supla.android"
        minSdk = Versions.MinSdk
        targetSdk = Versions.TargetSdk
        multiDexEnabled = true
        versionCode = 151
        versionName = "2.3.81"

        ndk {
            moduleName = "suplaclient"
            debugSymbolLevel = "FULL"
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        create("internaltest") {
            initWith(buildTypes.getByName("debug"))
            applicationIdSuffix = ".t"
        }
    }

    sourceSets {
        getByName("internaltest") {
            res.srcDir("internaltest/res")
        }
        getByName("main") {
            jniLibs.srcDir("src/main/libs")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }

    lint {
        checkReleaseBuilds = false
        disable.add("JvmStaticProvidesInObjectDetector")
        disable.add("FieldSiteTargetOnQualifierAnnotation")
        disable.add("ModuleCompanionObjects")
        disable.add("ModuleCompanionObjectsNotInModuleParent")
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    buildToolsVersion = "30.0.3"


    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(Dependencies.Multidex)
    implementation(Dependencies.Androidx.Lifecycle.Extensions)
    implementation(Dependencies.AndroidChart)
    implementation(Dependencies.RxJava.RxJava)
    implementation(Dependencies.RxJava.RxAndroid)
    implementation(Dependencies.GoogleMaterial)
    implementation(Dependencies.Androidx.ConstraintLayout)
    implementation(Dependencies.Androidx.Core.Core)
    implementation(Dependencies.Androidx.Core.Ktx)
    implementation(Dependencies.Androidx.Lifecycle.Runtime)
    implementation(Dependencies.Androidx.Lifecycle.Viewmodel)
    implementation(Dependencies.Kotlin)
    implementation(Dependencies.Coroutines)
    implementation(Dependencies.Androidx.Fragment)
    implementation(Dependencies.Androidx.Lifecycle.Livedata)
    implementation(Dependencies.Androidx.Navigation.Fragment)
    implementation(Dependencies.Androidx.Navigation.UI)
    implementation(Dependencies.Androidx.Navigation.DynamicFeatures)
    implementation(Dependencies.Androidx.Preferences)
    implementation(Dependencies.Androidx.Worker)
    implementation(Dependencies.Androidx.RecyclerView)
    implementation(Dependencies.Hilt.Hilt)
    kapt(Dependencies.Hilt.Kapt)

    testImplementation(Dependencies.Testing.Androidx.Core)
    testImplementation(Dependencies.Testing.Androidx.Runner)
    testImplementation(Dependencies.Testing.Androidx.Rules)
    testImplementation(Dependencies.Testing.Androidx.JUnitExtension)
    testImplementation(Dependencies.Testing.Androidx.ArchCore)
    testImplementation(Dependencies.Testing.Mockito.Core)
    testImplementation(Dependencies.Testing.Mockito.Kotlin)
    testImplementation(Dependencies.Testing.Robolectric)
    testImplementation(Dependencies.Testing.JUnit)
    testImplementation(Dependencies.Testing.Hamcrest)
    testImplementation(Dependencies.Testing.Coroutines)
    testImplementation(Dependencies.Testing.Mockk)
    androidTestImplementation(Dependencies.Testing.Androidx.Navigation)

    implementation(files("src/main/libs/jsoup-1.13.1.jar"))
}

kapt {
    correctErrorTypes = true
}

plugins {
    id("com.android.application")
    id("com.diffplug.spotless")
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
        versionCode = 168
        versionName = "23.02"

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
    implementation(Deps.Multidex)
    implementation(Deps.Androidx.Lifecycle.Extensions)
    implementation(Deps.AndroidChart)
    implementation(Deps.RxJava.RxJava)
    implementation(Deps.RxJava.RxAndroid)
    implementation(Deps.GoogleMaterial)
    implementation(Deps.Androidx.ConstraintLayout)
    implementation(Deps.Androidx.Core.Core)
    implementation(Deps.Androidx.Core.Ktx)
    implementation(Deps.Androidx.Lifecycle.Runtime)
    implementation(Deps.Androidx.Lifecycle.Viewmodel)
    implementation(Deps.Kotlin)
    implementation(Deps.Coroutines)
    implementation(Deps.Androidx.Fragment)
    implementation(Deps.Androidx.Lifecycle.Livedata)
    implementation(Deps.Androidx.Navigation.Fragment)
    implementation(Deps.Androidx.Navigation.UI)
    implementation(Deps.Androidx.Navigation.DynamicFeatures)
    implementation(Deps.Androidx.Preferences)
    implementation(Deps.Androidx.Worker)
    implementation(Deps.Androidx.RecyclerView)
    implementation(Deps.Hilt.Hilt)
    kapt(Deps.Hilt.Kapt)

    testImplementation(Deps.Testing.Androidx.Core)
    testImplementation(Deps.Testing.Androidx.Runner)
    testImplementation(Deps.Testing.Androidx.Rules)
    testImplementation(Deps.Testing.Androidx.JUnitExtension)
    testImplementation(Deps.Testing.Androidx.ArchCore)
    testImplementation(Deps.Testing.Mockito.Core)
    testImplementation(Deps.Testing.Mockito.Kotlin)
    testImplementation(Deps.Testing.Robolectric)
    testImplementation(Deps.Testing.JUnit)
    testImplementation(Deps.Testing.Hamcrest)
    testImplementation(Deps.Testing.Coroutines)
    testImplementation(Deps.Testing.Mockk)
    androidTestImplementation(Deps.Testing.Androidx.Navigation)

    implementation(files("src/main/libs/jsoup-1.13.1.jar"))
}

kapt {
    correctErrorTypes = true
}

spotless {
    ratchetFrom("origin/develop")

    java {
        target(fileTree("dir" to "src", "include" to "**/*.java"))

        googleJavaFormat("1.15.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        target(fileTree("dir" to "src", "include" to "**/*.kt"))
        ktlint(Versions.KtLint).editorConfigOverride(mapOf(
                "disabled_rules" to "no-wildcard-imports",
                "max_line_length" to "100",
                "indent_size" to "2"
        ))
    }
}
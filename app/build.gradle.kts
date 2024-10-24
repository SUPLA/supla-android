import java.util.Date

plugins {
  id("com.android.application")
  id("com.diffplug.spotless")
  kotlin("android")
  kotlin("kapt")
  id("com.google.gms.google-services")
  id("com.google.dagger.hilt.android") version Versions.Hilt
  id("org.jetbrains.kotlin.plugin.compose") version Versions.Kotlin
  kotlin("plugin.serialization") version "2.0.0"
}

android {
  compileSdk = Versions.Sdk
  buildToolsVersion = Versions.BuildTools
  namespace = "org.supla.android"

  useLibrary("android.test.runner")
  useLibrary("android.test.base")
  useLibrary("android.test.mock")

  defaultConfig {
    applicationId = "org.supla.android"
    minSdk = Versions.MinSdk
    targetSdk = Versions.TargetSdk
    multiDexEnabled = true
    versionCode = 253
    versionName = "24.10.03"

    ndk {
      moduleName = "suplaclient"
      debugSymbolLevel = "FULL"
    }

    val buildTime = Date()
    buildConfigField("Long", "BUILD_TIME", "${buildTime.time}L")
  }

  compileOptions {
    sourceCompatibility(JavaVersion.VERSION_17)
    targetCompatibility(JavaVersion.VERSION_17)
    isCoreLibraryDesugaringEnabled = true
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
    create("internalTestRelease") {
      initWith(buildTypes.getByName("release"))
      applicationIdSuffix = ".t"
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  sourceSets {
    getByName("internaltest") {
      res.srcDir("internaltest/res")
    }
    getByName("internalTestRelease") {
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

  buildFeatures {
    dataBinding = true
    viewBinding = true
    compose = true
    buildConfig = true
  }

  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs = listOf("-Xcontext-receivers", "-Xjvm-default=all")
  }
}

repositories {
  maven(url = "https://jitpack.io")
}

dependencies {
  implementation(Deps.Androidx.Core.Splash)
  implementation(Deps.Multidex)
  implementation(Deps.Androidx.Lifecycle.Extensions)
  implementation(Deps.AndroidChart)
  implementation(Deps.RxJava.RxJava)
  implementation(Deps.RxJava.RxAndroid)
  implementation(Deps.RxJava.RxKotlin)
  implementation(Deps.Retrofit.Retrofit)
  implementation(Deps.Retrofit.Gson)
  implementation(Deps.Retrofit.RxJavaAdapter)
  implementation(Deps.Retrofit.Logging)
  implementation(Deps.GoogleMaterial)
  implementation(Deps.Androidx.ConstraintLayout)
  implementation(Deps.Androidx.Core.Core)
  implementation(Deps.Androidx.Core.Ktx)
  implementation(Deps.Androidx.Lifecycle.Runtime)
  implementation(Deps.Androidx.Lifecycle.Viewmodel)
  implementation(Deps.Androidx.Room.Runtime)
  implementation(Deps.Androidx.Room.RxJava)
  implementation(Deps.Androidx.AppCompat)
  implementation(Deps.Androidx.Biometric)
  implementation(Deps.Kotlin)
  implementation(Deps.Coroutines)
  implementation(Deps.Androidx.Fragment)
  implementation(Deps.Androidx.Lifecycle.Livedata)
  implementation(Deps.Androidx.Navigation.Fragment)
  implementation(Deps.Androidx.Navigation.UI)
  implementation(Deps.Androidx.Preferences)
  implementation(Deps.Androidx.Worker)
  implementation(Deps.Androidx.RecyclerView)
  implementation(Deps.Hilt.Hilt)
  implementation(Deps.Hilt.Worker)
  implementation(Deps.FragmentViewBinding)
  implementation(platform(Deps.Firebase.Bom))
  implementation(Deps.Firebase.Messaging)
  implementation(Deps.Androidx.Security.Crypto)
  implementation(Deps.Androidx.Compose.UI)
  implementation(Deps.Androidx.Compose.Icons)
  implementation(Deps.Androidx.Compose.Material3.Core)
  implementation(Deps.Androidx.Compose.Material3.Adaptive)
  implementation(Deps.Androidx.Compose.Tooling)
  implementation(Deps.Androidx.Compose.ToolingPreview)
  implementation(Deps.Androidx.Compose.ConstraintLayout)
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

  annotationProcessor(Deps.Androidx.Room.Compiler)

  kapt(Deps.Hilt.Kapt)
  kapt(Deps.Hilt.WorkerKapt)
  kapt(Deps.Androidx.Room.Compiler)

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
  testImplementation(Deps.Testing.AssertJ)
  testImplementation(Deps.Testing.Kotlin.Reflect)
  androidTestImplementation(Deps.Testing.Androidx.Navigation)

  implementation(files("src/main/libs/jsoup-1.13.1.jar"))
}

kapt {
  correctErrorTypes = true
}

spotless {
  java {
    target(fileTree("dir" to "src", "include" to "**/*.java"))

    googleJavaFormat("1.19.2")
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlin {
    target(fileTree("dir" to "src", "include" to "**/*.kt"))
    ktlint(Versions.KtLint).editorConfigOverride(
      mapOf(
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_standard_filename" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "ktlint_experimental" to "disabled",
        "max_line_length" to "140",
        "indent_size" to "2"
      )
    )
  }
}

composeCompiler {
  enableStrongSkippingMode.set(true)
}


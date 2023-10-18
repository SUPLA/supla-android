plugins {
  id("com.android.application")
  id("com.diffplug.spotless")
  kotlin("android")
  kotlin("kapt")
  id("androidx.navigation.safeargs.kotlin")
  id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
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
    versionCode = 198
    versionName = "23.10.01-BETA1"

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
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Versions.Androidx.Compose.Core
  }

  kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs = listOf("-Xcontext-receivers", "-Xjvm-default=all")
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
  implementation(Deps.Hilt.Worker)
  implementation(Deps.FragmentViewBinding)
  implementation(platform(Deps.Firebase.Bom))
  implementation(Deps.Firebase.Messaging)
  implementation(Deps.Androidx.Security.Crypto)
  implementation(Deps.Androidx.Compose.UI)
  implementation(Deps.Androidx.Compose.Material)
  implementation(Deps.Androidx.Compose.Tooling)
  implementation(Deps.Androidx.Compose.ConstraintLayout)

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
  androidTestImplementation(Deps.Testing.Androidx.Navigation)

  implementation(files("src/main/libs/jsoup-1.13.1.jar"))
}

kapt {
  correctErrorTypes = true
}

spotless {
  java {
    target(fileTree("dir" to "src", "include" to "**/*.java"))

    googleJavaFormat("1.15.0")
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlin {
    target(fileTree("dir" to "src", "include" to "**/*.kt"))
    ktlint(Versions.KtLint).editorConfigOverride(
      mapOf(
        "disabled_rules" to "no-wildcard-imports, filename",
        "max_line_length" to "140",
        "indent_size" to "2"
      )
    )
  }
}


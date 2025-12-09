import java.util.Date

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.google.services)
  alias(libs.plugins.hilt)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

android {
  compileSdk = libs.versions.compileSdk.get().toInt()
  buildToolsVersion = libs.versions.buildTools.get()
  namespace = "org.supla.android"

  useLibrary("android.test.runner")
  useLibrary("android.test.base")
  useLibrary("android.test.mock")

  defaultConfig {
    applicationId = "org.supla.android"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    multiDexEnabled = true
    versionCode = 310
    versionName = "25.12"

    ndk {
      moduleName = "suplaclient"
      debugSymbolLevel = "FULL"
    }

    val buildTime = Date()
    buildConfigField("Long", "BUILD_TIME", "${buildTime.time}L")
  }

  compileOptions {
    sourceCompatibility(JavaVersion.VERSION_21)
    targetCompatibility(JavaVersion.VERSION_21)
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
    jvmTarget = "21"
    freeCompilerArgs = listOf("-Xcontext-receivers", "-Xjvm-default=all")
  }
  packaging {
    jniLibs {
      useLegacyPackaging = false
    }
  }
}

dependencies {
  implementation(project(":shared-core"))

  implementation(libs.multidex)
  implementation(libs.androidChart)
  implementation(libs.googleMaterial)
  implementation(libs.coroutines.android)
  implementation(libs.coroutines.rx3)
  implementation(libs.fragmentViewBinding)

  implementation(libs.hilt)
  implementation(libs.hilt.worker)

  implementation(libs.androidx.constraintLayout)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.preferences)
  implementation(libs.androidx.recyclerView)
  implementation(libs.androidx.appCompat)
  implementation(libs.androidx.biometric)
  implementation(libs.androidx.worker)
  implementation(libs.androidx.core)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splash)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.livedata)
  implementation(libs.androidx.lifecycle.extensions)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.rxjava)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.security.crypto)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.activity)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.toolingPreview)
  implementation(libs.androidx.compose.material.icons)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.adaptive)
  implementation(libs.androidx.compose.constraintLayout)
  implementation(libs.androidx.car.app)
  implementation(libs.androidx.glance.widget)
  implementation(libs.androidx.glance.widget.preview)
  implementation(libs.androidx.glance.material)
  implementation(libs.androidx.glance.preview)

  implementation(libs.rxjava)
  implementation(libs.rxandroid)
  implementation(libs.rxkotlin)
  implementation(libs.retrofit)
  implementation(libs.retrofit.gson)
  implementation(libs.retrofit.rxJavaAdapter)
  implementation(libs.retrofit.logging)

  implementation(platform(libs.firebase))
  implementation(libs.firebase.messaging)

  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.serialization)
  implementation(libs.jsoup)
  implementation(libs.coil)
  implementation(libs.guava)
  implementation(libs.reorderable)
  implementation(libs.google.barcode.scanner)
  implementation(libs.timber)

  coreLibraryDesugaring(libs.android.tools.desugar)

  ksp(libs.hilt.kapt)
  ksp(libs.hilt.worker.kapt)
  ksp(libs.androidx.room.compiler)

  testImplementation(libs.testing.junit)
  testImplementation(libs.testing.hamcrest)
  testImplementation(libs.testing.coroutines)
  testImplementation(libs.testing.mockk)
  testImplementation(libs.testing.assertj)
  testImplementation(libs.testing.mockito)
  testImplementation(libs.testing.mockito.kotlin)
  testImplementation(libs.testing.androidx)
  testImplementation(libs.testing.androidx.navigation)
  testImplementation(libs.testing.androidx.runner)
  testImplementation(libs.testing.androidx.rules)
  testImplementation(libs.testing.androidx.junit)
  testImplementation(libs.testing.androidx.arch.core)
  testImplementation(libs.testing.kotlin.relfect)
}

spotless {
  java {
    target(fileTree("dir" to "src", "include" to "**/*.java"))

    googleJavaFormat(libs.versions.googleJavaFormat.get())
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
  kotlin {
    target(fileTree("dir" to "src", "include" to "**/*.kt"))
    ktlint().editorConfigOverride(
      mapOf(
        "ktlint_standard_no-wildcard-imports" to "disabled",
        "ktlint_standard_filename" to "disabled",
        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
        "max_line_length" to "140",
        "indent_size" to "2",
        // After update from 0.48.1 to 1.8.0
        "ktlint_code_style" to "intellij_idea",
        "ktlint_function_signature_body_expression_wrapping" to "default",
        "ktlint_standard_function-expression-body" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
        "ktlint_standard_function-signature" to "disabled",
        "ktlint_standard_enum-wrapping" to "disabled",
        "ktlint_standard_value-parameter-comment" to "disabled",
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable"
      )
    )
  }
}


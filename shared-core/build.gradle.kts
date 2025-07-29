import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.skie)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "org.supla.core.shared"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
    create("internaltest") {
      initWith(buildTypes.getByName("debug"))
    }
    create("internalTestRelease") {
      initWith(buildTypes.getByName("release"))
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}

kotlin {
  androidTarget()

  val xcf = XCFramework("SharedCore")

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "SharedCore"
      xcf.add(this)
    }
  }

  sourceSets {
    val commonMain by getting
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting

    commonMain.dependencies {
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlin.bitops.endian)
    }
  }
}

dependencies {
  testImplementation(libs.testing.junit)
  testImplementation(libs.testing.mockk)
  testImplementation(libs.testing.assertj)
}

tasks.register<Exec>("applyXCFramework") {
  commandLine("cp", "-R", "build/XCFrameworks/release/SharedCore.xcframework", "../../supla-ios")
  dependsOn("assembleSharedCoreReleaseXCFramework")
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
    ktlint(libs.versions.ktlint.get()).editorConfigOverride(
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
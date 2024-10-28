import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
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
  }
}

dependencies {
  testImplementation(libs.testing.junit)
}

tasks.register<Exec>("applyXCFramework") {
  commandLine("cp", "-R", "build/XCFrameworks/release/SharedCore.xcframework", "../../supla-ios")
  dependsOn("assembleSharedCoreReleaseXCFramework")
}
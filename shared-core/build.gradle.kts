import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  kotlin("multiplatform")
  id("com.android.library")
}

android {
  namespace = "org.supla.core.shared"
  compileSdk = 34

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
    }
    create("internaltest") {
      initWith(buildTypes.getByName("debug"))
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  androidTarget()

  val xcf = XCFramework()

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "shared-core"
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
  testImplementation("junit:junit:4.13.2")
}

tasks.register<Exec>("applyXCFramework") {
  commandLine("cp", "-R", "build/XCFrameworks/release/shared_core.xcframework", "../../supla-ios")
  dependsOn("assembleXCFramework")
}
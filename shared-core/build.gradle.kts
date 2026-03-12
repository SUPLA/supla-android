import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.multiplatform.library)
  alias(libs.plugins.skie)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    namespace = "org.supla.core.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()
    minSdk = libs.versions.minSdk.get().toInt()

    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
    }

    withHostTest {  }
  }

  jvmToolchain(21)

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
      implementation(libs.kotlin.bitops.endian)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
    }

    getByName("androidHostTest").dependencies {
      implementation(libs.testing.junit)
      implementation(libs.testing.mockk)
      implementation(libs.testing.assertj)
    }
  }
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
        "ktlint_standard_blank-line-between-when-entries" to "disabled",
        "ij_kotlin_line_break_after_multiline_when_entry" to "false"
      )
    )
  }
}
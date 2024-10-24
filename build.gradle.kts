// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kapt) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.ksp) apply false
}

tasks.register("clean").configure {
  delete(rootProject.layout.buildDirectory)
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("com.google.dagger.hilt.android") version Versions.Hilt apply false
}

buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath(Deps.Hilt.Classpath)
    classpath(Deps.KotlinGradlePlugin)
    classpath(Deps.AndroidGradlePlugin)
    classpath(Deps.Spotless)
    classpath(Deps.GoogleServices)

    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
  }
}

tasks.register("clean").configure {
  delete(rootProject.layout.buildDirectory)
}

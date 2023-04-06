// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(Deps.Hilt.Classpath)
        classpath(Deps.KotlinGradlePlugin)
        classpath(Deps.AndroidBuildTools)
        classpath(Deps.Androidx.Navigation.SafeArgsGradlePlugin)
        classpath(Deps.Spotless)

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
    delete(rootProject.buildDir)
}

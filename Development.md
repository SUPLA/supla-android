# SUPLA Android – Development

This document describes how to build and work with the **SUPLA Android** application in a local development environment.

It applies to contributors and developers working on the mobile application source code.

---

## Build

You can use the Gradle wrapper to build the project.

Build a test APK:

```bash
./gradlew assembleInternaltest
```

The APK will be generated in:

* `app/build/outputs/apk/internaltest/`

Install it on a device (example):

```bash
adb install -r path/to/app-internaltest.apk
```

---

## Tests

Run available tests and checks:

```bash
./gradlew connectedCheck
```

---

## Code formatting

This repository uses **Spotless** to keep formatting consistent.

Check formatting:

```bash
./gradlew spotlessCheck
```

Apply formatting:

```bash
./gradlew spotlessApply
```

Note: Spotless is configured to focus on changes introduced on your branch.

---

## Notes

* The Android app communicates with SUPLA server and SUPLA Cloud.
* Most functionality requires access to a running SUPLA server and SUPLA Cloud instance (official or self-hosted).
* Backend services are not part of this repository.


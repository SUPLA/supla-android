# Contributing to SUPLA Android

Thank you for your interest in contributing to **SUPLA Android**.
This document describes the rules and guidelines for contributing code,
documentation, and ideas to the project.

## Table of contents

* [Contributor License Agreement (CLA)](#contributor-license-agreement-cla)
* [Project scope and goals](#project-scope-and-goals)
* [Supported environments](#supported-environments)
* [Reporting issues](#reporting-issues)
* [Feature requests](#feature-requests)
* [Development guidelines](#development-guidelines)
* [Pull request process](#pull-request-process)
* [Licensing](#licensing)

---

## Contributor License Agreement (CLA)

This project requires acceptance of a **Contributor License Agreement (CLA)**.

Before a pull request can be merged, the CLA must be accepted by the contributor.

Pull requests with an unaccepted CLA will be blocked automatically.

---

## Project scope and goals

`supla-android` is the **official Android application** for the SUPLA smart home platform.

Key goals:

* stable and predictable user experience,
* clear and maintainable architecture,
* performance and battery efficiency.
* UI/UX consistency and feature parity with the SUPLA iOS app

The Android app is a **client** of SUPLA server.
Changes should not assume a specific server deployment (official vs self-hosted).

---

## Supported environments

* Android Studio (latest stable recommended)
* Android devices and emulators supported by the current app configuration

Build requirements are defined by the project (Gradle, Android Gradle Plugin, Kotlin).

---

## Reporting issues

Before opening an issue:

1. Search existing issues (open and closed).
2. Verify that you are using a recent app version.

When reporting a bug, please include:

* app version (release tag, build number, or commit),
* device model and Android version,
* steps to reproduce,
* expected vs actual behavior,
* logs if available (sanitize secrets).

If the issue depends on the backend, specify whether you use:

* the official SUPLA server, or
* a self-hosted instance.

Security-related issues must not be reported via public issues.
See `SECURITY.md`.

---

## Feature requests

Feature requests are welcome.

Please describe:

* the problem you are trying to solve,
* why existing functionality is insufficient,
* any UI/UX expectations,
* whether the change depends on SUPLA Server / API behavior.

Large changes should be discussed in an issue before implementation.

---

## Development guidelines

* Keep changes small and focused.
* Follow existing app architecture and patterns.
* Maintain UX consistency.
* Be mindful of performance, battery usage, and background behavior.
* Avoid breaking compatibility without prior discussion.
* Run formatting checks (Spotless) before submitting a PR.

---

## Pull request process

1. Fork the repository and create a feature branch.
2. Make small, focused commits with clear commit messages.
3. Ensure the app builds and checks pass.
4. Open a pull request using the provided template.
5. Ensure the CLA check passes.

Each pull request should:

* address a single issue or feature,
* include a clear description of the changes,
* reference related issues if applicable.

---

## Licensing

By contributing to this repository, you agree that your contributions
will be licensed under the same license as the project.

Any third-party code must use a compatible license and be clearly documented.


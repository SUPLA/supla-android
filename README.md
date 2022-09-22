# SUPLA project

[SUPLA](https://www.supla.org) is an open source project for home automation.

# Supla Android

This repository contains source code of Supla Android application.

## Installation

You can use Gradle wrapper to build the project. For test purpose use the following command to get
working test application

```
gradlew assembleInternaltest
```

Application will be generated in `app/build/outputs/apk/internaltest/` directory. Select proper
architecture and copy on your phone. You can use also `adb` to install the file directly on the
connected phone.

## How to test the software

Currently there are only few unit tests written. To start them all you can use Gradle task:

```
gradlew connectedCheck
```

## When you contribute...

Please follow the rules below to keep the app consistent between other contributors.

### Code formatting

We want to keep the new code consistent. For that the spotless tool was introduced. Please always
keep in mind to run `gradlew spotlessCheck` to be sure that your newly written code does not brake
the rules.

Please correct all warning found by spotless. If you don't want to make manual correction try to
use `gradlew spotlessApply` to let spotless format the code.

As there is currently a lot of legacy code in the repository, the spotless is configured to only
compare your new changes on the branch. But this configuration will only work correctly if your
remote is named 'origin' and (if you forked) when your develop branch is up to date with the
original one.
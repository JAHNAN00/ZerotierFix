# ZerotierFix

## Build And Test

- Use JDK 17. This project uses Android Gradle Plugin 7.4.2 with Gradle 7.5; the Android Studio JBR 21 fails during the `jlink` transform.
- The Android SDK must provide API 33, CMake 3.22.1, and an NDK. `core` builds JNI code for the app.
- Build the debug APK with `./gradlew :app:assembleDebug` (`.\gradlew.bat :app:assembleDebug` on Windows). The APK is `app/build/outputs/apk/debug/app-debug.apk`.
- Run local unit tests with `./gradlew :app:testDebugUnitTest`; run device tests with `./gradlew :app:connectedDebugAndroidTest` and a connected device.

## Project Structure

- `app` is the Android client. Its application ID and namespace are both `net.jahnan00.zerotierfix`; keep them, the manifest application class, layout fragment class names, tests, and the GreenDAO `daoPackage` aligned when renaming packages.
- `core` is the Android library wrapper. It compiles Java SDK sources from `externals/core/java/src` and native code from `externals/core/java/CMakeLists.txt`.
- `externals/core` is a required git submodule. For a fresh checkout run `git submodule update --init --recursive`; do not replace its upstream URL merely when rebranding the app.
- `ZeroTierOneService` is the VPN service and `NetworkListActivity` is the launcher activity. VPN behavior requires user approval on the device.

## Workflow Gotchas

- GreenDAO runs during the Android build. Keep `schemaVersion` and `daoPackage` in `app/build.gradle` consistent with model changes and the app namespace.
- After changing `applicationId` or the namespace, sync Gradle in Android Studio before running. Otherwise the IDE can launch a stale package ID even when the APK builds successfully.
- CI checks out submodules recursively and builds the debug APK; push and PR workflows target `master`.

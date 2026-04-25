# ML Kit Showcase

ML Kit Showcase is a small Android app for trying a few ML Kit scanning flows in one place. It is meant to be a readable demo, not a production scanner SDK.

The app is built with Kotlin, Jetpack Compose, CameraX, ML Kit, Hilt, and the Gradle version catalog in `gradle/libs.versions.toml`.

## What It Demonstrates

- Live barcode scanning from the camera.
- Barcode image import from the Android content picker.
- Structured barcode payload mapping for URLs, Wi-Fi, contacts, phone numbers, SMS, email, calendar events, geo points, ISBNs, and plain text.
- Live Latin-script text recognition from the camera.
- Text image import from the Android content picker.
- Text block and line counts, plus copy and share actions for recognized text.
- ML Kit Document Scanner through Google Play services, returning page image URIs and a PDF URI when available.
- A session-only history list for the last five scan results.

## ML Kit Behavior

Barcode scanning and text recognition use bundled ML Kit libraries:

- `com.google.mlkit:barcode-scanning`
- `com.google.mlkit:text-recognition`

Those features are available immediately after install and the app does not request internet or network-state permissions for them.

Document scanning uses:

- `com.google.android.gms:play-services-mlkit-document-scanner`

That scanner is provided by Google Play services. It can be unavailable on unsupported devices or while Play services is still setting up its scanner module. The app shows a normal error state for those cases.

## Run The App

Open the project in Android Studio, let Gradle sync, and run the `app` configuration on an emulator or device running Android 7.0 or newer.

The camera permission is requested only when you start live camera scanning. Image import and document scanning do not require the app-level camera permission.

## Build And Test

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

For Android test source compilation:

```bash
./gradlew compileDebugAndroidTestKotlin
```

## Current Build Setup

- Min SDK: 24
- Compile SDK: 37
- Target SDK: 37
- Java target: 17
- AGP: 9.2.0
- Kotlin: 2.3.21
- Compose BOM: 2026.04.01
- CameraX: 1.6.0
- Hilt: 2.59.2

Dependency versions live in the version catalog, so check `gradle/libs.versions.toml` for the exact current set.

## Project Shape

```text
app/src/main/java/com/vamsi/mlkitshowcase/
├── data/scanner/          ML Kit scanner wrappers and SDK mappers
├── domain/model/          Scan result, payload, text, document, and history models
├── presentation/barcode/  Barcode scanner screen and state
├── presentation/text/     Text recognition screen and state
├── presentation/document/ Document scanner screen and state
├── presentation/home/     Home screen and session history
├── presentation/components/
│   ├── CameraPreview.kt
│   ├── DetectedBoundsOverlay.kt
│   └── ScannerActionPrompt.kt
└── di/                    Hilt bindings
```

## Notes And Limitations

- Text recognition uses ML Kit's Latin-script recognizer.
- The app displays ML Kit-provided text structure. It does not invent confidence scores.
- Recent scans are kept in memory only. Closing the app clears them.
- The scanner UI is designed for demo clarity. It does not include persistence, analytics, account state, or backend services.
- Camera and scanner behavior still needs manual QA on real devices for focus, lighting, rotation, and Google Play services availability.

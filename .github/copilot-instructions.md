# Copilot instructions for this repository

## Big picture
- This is a multi-module Android sample workspace for ProGlove integrations, not a single app.
- Modules in `settings.gradle`:
  - `pgSdkSampleApp` (Kotlin, full SDK sample)
  - `pgSdkSampleJavaApp` (Java, full SDK sample)
  - `pgIntentSampleApp` (Kotlin, full Intent API sample)
  - `pgSlimIntentSampleApp` / `pgSlimIntentSampleJavaApp` (minimal Intent API samples)
  - `common` (shared constants/sample data for full samples)
- Keep changes scoped to the target sample module unless explicitly asked to mirror across Kotlin/Java variants.

## Integration boundaries and data flow
- Intent API path (`pgIntentSampleApp`):
  - `IntentActivity` wires UI and lifecycle.
  - `MessageHandler` is the broadcast boundary (registers filter, parses actions/extras, dispatches callbacks).
  - Use `common/src/main/java/de/proglove/example/common/ApiConstants.kt` for actions/extras.
- SDK path (`pgSdkSampleApp`, `pgSdkSampleJavaApp`):
  - `SdkActivity` owns a `PgManager`, subscribes to SDK outputs, sends typed SDK commands.
  - Before SDK command paths when connection state may be unknown, call `ensureConnectionToService(...)` first (safe no-op when already connected).
- Display V2 differs by API:
  - Intent sample sends JSON payloads in intent extras (`DisplayV2Examples.kt`).
  - SDK sample builds typed models (`PgScreen`, `PgScreenView`, `PgActionButtons`, etc.).

## Build and run workflow
- Primary local build: `./gradlew assembleDebug`
- Build one sample module quickly:
  - `./gradlew :pgIntentSampleApp:assembleDebug`
  - `./gradlew :pgSdkSampleApp:assembleDebug`
- There are no `src/test` or `src/androidTest` suites in this repo; validate by assembling the affected module.
- `VERSION_CODE` env var overrides app versionCode for non-slim apps (root `build.gradle` + module `build.gradle`).
- Kotlin full samples (`pgSdkSampleApp`, `pgIntentSampleApp`) optionally load release signing from root `keystore.properties`.

## Project-specific conventions
- Kotlin modules use ViewBinding heavily (`buildFeatures { viewBinding = true }`) and update UI via binding fields.
- Intent receiver lifecycle pattern (full and slim intent samples):
  - Register receiver in `onCreate` (Android 13+: `RECEIVER_EXPORTED`).
  - Process startup intent immediately (`handleNewIntent(intent)`), not only `onNewIntent`.
  - Unregister in `onDestroy`.
- SDK lifecycle pattern:
  - Subscribe in `onCreate`.
  - Call `ensureConnectionToService(applicationContext)` in `onResume`.
  - Unsubscribe in `onDestroy` (Kotlin sample unsubscribes all listeners; Java sample unsubscribes core listeners and follows the same connection pattern).

## Dependency and environment notes
- Repositories are centralized in `settings.gradle` (`RepositoriesMode.FAIL_ON_PROJECT_REPOS`), including ProGlove Cloudsmith Maven.
- Use the SDK dependency/version strategy already defined in each module `build.gradle`; do not rewrite or bump versions unless explicitly requested.
- Keep existing Android/JVM toolchain values in module Gradle files unchanged unless the task is explicitly a version migration.
- Some features (for example device visibility) depend on app/environment setup (including licensing in Insight Mobile); treat callback/status errors as integration-state issues, not automatic SDK-code defects.

## Editing guidance for agents
- Prefer extending existing handler/callback flows instead of creating parallel receiver/service abstractions.
- Reuse `ApiConstants` in full intent samples; keep slim samples intentionally minimal and self-contained.
- Preserve sample-app clarity: straightforward callbacks, explicit Toast/status text, and simple UI wiring over abstraction-heavy refactors.
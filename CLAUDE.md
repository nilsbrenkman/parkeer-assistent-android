# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

The **Android client** for ParkeerAssistent — an app for Amsterdam visitor parking permits, being
ported from the SwiftUI iOS app. It is a Jetpack Compose app that talks **only to the sibling Ktor
`../server/`** backend (never to Egis directly), so the API contract is fixed by the server; this
is a client + UI reimplementation of the iOS app, not a redesign.

The port is largely complete (login, parking, visitors, history, payment, settings, accounts,
info; notifications, saved credentials + biometrics, location/meter picker, Stats/Play review,
i18n, app icon). **`docs/ANDROID_PORT_PLAN.md` is the source of truth for phase-by-phase status,
decisions, and deviations — read it before non-trivial work.** Remaining: Phase 8 (tests + polish).

Monorepo context (parent dir `../`): `../server/` (Ktor BFF — see `../server/CLAUDE.md` for the API
contract, cookie session, and mock mode), `../ios/` (the reference implementation — mirror its
behavior/screens), plus `../mp`, `../orig`, `../assets`, `../k8s`.

## Commands

Gradle wrapper (`./gradlew`), **JDK 21**. SDK path in git-ignored `local.properties`. No product
flavors.

| Command | Purpose |
| --- | --- |
| `./gradlew assembleDebug` | Build the debug APK |
| `./gradlew installDebug` | Build + install on a connected device/emulator |
| `./gradlew test` | JVM unit tests (`app/src/test`) |
| `./gradlew test --tests "nl.parkeerassistent.amsterdam.SomeTest"` | Single test class |
| `./gradlew connectedAndroidTest` | Instrumented/Compose tests (needs a device) |
| `./gradlew lint` | Android Lint |

**Running/verifying on the emulator** (SDK tools under `~/Library/Android/sdk/{platform-tools,emulator}`, not on PATH):
boot the `PAX_A920Pro` AVD headless (`emulator -avd PAX_A920Pro -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect &`),
`adb wait-for-device`, `installDebug`, launch `nl.parkeerassistent.amsterdam/.MainActivity`. The
build hits the **public** server; for deterministic data the `X-ParkeerAssistent-Mock: true` header
(in `AnalyticsHeadersInterceptor`) uses the server's mock mode — **login `test` / `1234`**. OkHttp
BODY logging + Koin definitions print to logcat (grep `okhttp.OkHttpClient`, `[Koin]`, `FATAL`).

## Architecture

`nl.parkeerassistent.amsterdam`. Flow: **Compose screen → ViewModel (`StateFlow`) → repository →
Retrofit/OkHttp → server**. DI is **Koin**; navigation is **Navigation-Compose**.

- `data/model/` — `@Serializable` data classes (copied from the server's `model/*.kt`, which is
  more current than iOS `Model.swift`; IDs are `Long`).
- `data/remote/` — `ApiException`, interceptors (`AnalyticsHeaders`, `Error`), `cookie/` cookie jar,
  and the 6 Retrofit `*Api` interfaces. `data/repository/` — one repo per domain (interface + impl).
  `data/local/CredentialStore` (EncryptedSharedPreferences).
- `di/` — Koin modules: `networkModule`, `dataModule`, `viewModelModule` (started in
  `ParkeerAssistentApp`).
- `ui/` — `theme/` (`AppColors`/`AppTheme.colors`, `Dimens`, `AppType`), `components/` (reusable
  composables), `navigation/` (`Screen` sealed interface + `AppNavHost`), `screen/` (feature
  screens), `common/` (`MessageBus`, `ApiErrorHandler`), and per-feature ViewModels
  (`ui/session`, `ui/user`, `ui/visitor`, `ui/parking`, `ui/payment`, `ui/account`, `ui/parkingmeter`).
- `notifications/`, `location/`, `stats/`, `review/`, `auth/` (biometrics), `util/`.

**Session is cookie-based:** the server needs a `token` cookie (set on login) **and** a
`product_id` cookie (set by `GET user` — so call `getUser` before parking/balance work).
`SessionCookieJar` persists both; 401/403 → `ApiException.Unauthorized` clears them and
`SessionViewModel` drops the session.

## Conventions & patterns

- **Screens are split** into a stateless `private @Composable XxxContent(state, callbacks)` + a
  public `XxxScreen(vm = koinViewModel())` wrapper that collects `StateFlow`s and delegates. This
  keeps screens previewable (no Koin needed) and testable. Add a `@Preview` of the `Content`.
- **Shared ViewModels** (user/visitor/parking/account) are obtained once in `AppNavHost` and passed
  down, so destinations share one instance. `koinViewModel()` inside a `composable {}` would scope
  per nav entry — don't rely on that for cross-screen state.
- **Strings:** composables use `stringResource(R.string.…)`; non-composables (ViewModels,
  `ApiErrorHandler`, `Biometrics`) use `util/StringProvider` (Koin singleton). i18n lives in
  `res/values/` (**English, default**) + `res/values-nl/` (Dutch); the app follows device locale.
- **Errors/messages:** repos let `ApiException` propagate; ViewModels catch and call
  `ApiErrorHandler.handle()` (→ snackbar via `MessageBus`, or `unauthorized` flow). User-facing
  confirmations are currently snackbars (no iOS-style OK dialogs yet).
- **Swipe actions:** `SwipeToActionRow` (in `UserScreen`) — visitor swipe-to-delete, parking
  swipe-to-stop (no buttons, matching iOS `swipeActions`).
- Version catalog (`gradle/libs.versions.toml`) for all deps — reference as `libs.*`.

## Critical gotchas (this toolchain is bleeding-edge: AGP 9.2 / Kotlin 2.2.10)

- **Do NOT use Hilt or KSP.** Hilt's Gradle plugin fails on AGP 9 (`Android BaseExtension not
  found`); KSP collides with AGP's built-in Kotlin. DI is **Koin** (no plugin). If a future feature
  truly needs KSP (Room, Koin annotations), set `android.disallowKotlinSourceSets=false` in
  `gradle.properties` and expect friction. (See the `agp9-toolchain-constraints` memory.)
- **Kotlin nests block comments:** a `/*` or `*/` substring inside a KDoc (e.g. a path like
  `client/*Client.swift` or `res/values*/…`) breaks compilation. Avoid those substrings in comments.
- **material-icons isn't bundled** with material3 here — use text glyphs (`←`, `✓`, `≡`) not
  `Icons.*`.
- **No Google Maps API key** — the parking-meter picker is a distance-sorted *list*, not a map.
- **`BuildConfig.VERSION_NAME/CODE` aren't generated** — read version from `PackageManager`.
- **Not testable on the bare emulator:** biometric login (no enrolled fingerprint → degrades to
  manual), notifications (fire at real parking times), Play In-App Review (needs Play distribution).

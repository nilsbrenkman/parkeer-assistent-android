# TO-DO — Google Play submission

Open items before this app can be submitted to Google Play. See
`docs/ANDROID_PORT_PLAN.md` for the feature port status (Phases 0–8); this file tracks the
**release-engineering / Play Console** work that remains.

Legend: ✅ done · ⬜ open · 🔲 deferred polish (not a blocker)

## Submission blockers

- ✅ **1 — Release signing config.** `app/build.gradle.kts` reads a git-ignored
  `keystore.properties` (template: `keystore.properties.template`) and signs `release` when
  present, else leaves it unsigned. `.gitignore` excludes `keystore.properties`/`*.jks`/`*.keystore`.
  - ⬜ **Still need to create the actual upload keystore** (`keytool` cmd in the template) and fill
    in `keystore.properties`, then enroll in Play App Signing.
- ✅ **2 — App Bundle.** `./gradlew bundleRelease` produces the Play `.aab`
  (`app/build/outputs/bundle/release/app-release.aab`). Verified building.
- ✅ **7 — R8 / minification.** `isMinifyEnabled` + `isShrinkResources` on for `release`; keep
  rules in `proguard-rules.pro` cover kotlinx.serialization models, Retrofit `*Api` interfaces,
  OkHttp optional providers; line numbers kept for readable crash traces. Verified `bundleRelease`
  passes R8 and emits `mapping.txt`.
- ⬜ **3 — `targetSdk`/`compileSdk = 37` acceptance.** Confirm SDK 37 is a finalized, Play-accepted
  API level at submission time. Play rejects uploads targeting a preview/non-released SDK. (Risk
  area on this AGP 9.2 / bleeding-edge toolchain.)
- ⬜ **4 — `SCHEDULE_EXACT_ALARM` permission.** Play-restricted. Either file the Play Console
  declaration justifying it, or **drop the permission** (code already falls back to inexact alarms).
  Decision needed; dropping is the cleaner path.
- ⬜ **5 — Privacy Policy + Data Safety form.** App collects login credentials, precise location,
  and a per-install device identifier (`X-ParkeerAssistent-UserId`), and uses biometrics. Need:
  - Hosted privacy policy URL.
  - Play Data Safety questionnaire (credentials / location / identifiers + encryption in transit).
  - Location permission justification for `ACCESS_COARSE_LOCATION` (approximate location only).
    `ACCESS_FINE_LOCATION` was **dropped** — the meter picker only needs approximate location to
    centre the map; `LocationProvider` already uses `PRIORITY_BALANCED_POWER_ACCURACY`. No
    background location, so no Play Location Permissions Declaration form is required.
- ⬜ **6 — Store listing assets.** Feature graphic, phone screenshots, short/full description,
  content-rating questionnaire, category. (App icon exists.)
- ⬜ **11 — Mock mode off for release.** `AnalyticsHeadersInterceptor` currently sends
  `X-ParkeerAssistent-Mock: true`. Confirm it's removed/disabled in the release build (shipping it
  would point real users at fixtures). Consider giving Play review the `test` / `1234` account
  instead. **Arguably a hard blocker.**

## Quality / risk (should address)

- ⬜ **8 — Versioning.** `versionCode = 1` / `versionName = "1.0"` — confirm intentional for first
  release. Note `BuildConfig.VERSION_*` isn't generated; app reads version from `PackageManager`.
- ⬜ **9 — Backup rules.** `backup_rules.xml` / `data_extraction_rules.xml` are stock templates
  while `allowBackup="true"`. Keystore-encrypted `CredentialStore` can't restore across devices and
  session cookies shouldn't be backed up — exclude those prefs or set `allowBackup="false"`.
- ⬜ **10 — Real-device verification.** Biometric login, local notifications (fire at real parking
  times), and Play In-App Review aren't exercisable on the bare emulator. Validate via an
  internal-testing track build.
- ⬜ **security-crypto deprecation.** Release build warns on `EncryptedSharedPreferences`/`MasterKey`
  (deprecated upstream). Not a blocker; follow up on a replacement for credential storage.

## Deferred polish (not blockers)

- ✅ iOS-style confirmation **dialogs** vs current snackbars — keeping snackbars; no change wanted.
- ✅ **Self-hosted map tiles (server/k8s).** The meter picker is a **MapLibre map** (mirrors iOS
  MapKit; no Google Maps key needed) reading `BuildConfig.MAP_STYLE_URL` =
  `https://parkeerassistent.nl/tiles/styles/amsterdam/style.json`. Tiles are served by a custom
  tileserver-gl image with a baked Amsterdam Protomaps extract + style (`../../k8s/tileserver/`,
  image `nilsbrenkman/amsterdam-pmtiles`), behind the `parkeerassistent.nl/tiles/` ingress.
  Confirmed rendering on a real device.
- 🔲 Payment **success-poll / deep-link return** — balance refresh relies on the home-screen 60s
  loop rather than detecting return from the payment browser.

# Android Port Plan — ParkeerAssistent

Porting the SwiftUI iOS client (`../ios/`) to this Android/Compose module. The app talks
**only to the sibling Ktor `../server/`** backend (never to Egis directly), so the API
contract is fixed by the server and this is a client + UI reimplementation, not a redesign.

## Locked decisions

- **UI:** Jetpack Compose + Material 3 (already configured).
- **Networking:** Retrofit + OkHttp (+ kotlinx.serialization converter).
- **DI:** **Koin** (originally Hilt, but Hilt's Gradle plugin is incompatible with this project's
  AGP 9.2 — `Android BaseExtension not found`. Koin is pure-runtime, no Gradle plugin, so it works.
  Same reason we avoid KSP-based tooling: AGP 9.2 *built-in Kotlin* disallows the `kotlin.sourceSets`
  DSL KSP needs, requiring an `android.disallowKotlinSourceSets=false` opt-out. Prefer Koin /
  non-KSP options unless that flag is reintroduced.)
- **Async/state:** Coroutines + `StateFlow`, MVVM (`ViewModel` per former store).
- **Navigation:** Navigation-Compose with a sealed-class route type mirroring iOS `Screen`.

## Target package layout (`nl.parkeerassistent.amsterdam`)

```
data/
  model/        kotlinx.serialization data classes (port of util/Model.swift)
  remote/       Retrofit service interfaces (Login/User/Visitor/Parking/Payment/Geo)
  remote/dto/   request bodies
  CookieStore   persistent token + product_id cookie jar
  repository/   one repository per domain (port of client/*Client.swift)
di/             Hilt modules (NetworkModule, AppModule)
ui/
  theme/        (exists)
  components/   reusable composables (port of components/*)
  screen/       feature screens (port of view/*) + their ViewModels
  navigation/   Screen sealed class + NavHost
  common/       message/snackbar bus, error handling
platform/       credentials (Keystore), notifications, location, stats/review
util/           Log, License, date/format helpers
```

## API surface (from `client/*Client.swift`) — 6 Retrofit interfaces

| Domain | Method + path | Request → Response |
| --- | --- | --- |
| Login | `GET login` | → `Response` (loggedIn check) |
| Login | `POST login` | `LoginRequest` → `Response` |
| Login | `GET logout` | → `Response` |
| User | `GET user` | → `UserResponse` (sets `product_id`) |
| User | `GET user/balance` | → `BalanceResponse` |
| User | `GET user/regime/{parkingMeterId}` | → `RegimeResponse` |
| Visitor | `GET visitor` | → `VisitorResponse` |
| Visitor | `POST visitor` | `AddVisitorRequest` → `Response` |
| Visitor | `DELETE visitor/{id}` | → `Response` |
| Parking | `GET parking` | → `ParkingResponse` |
| Parking | `POST parking` | `AddParkingRequest` → `Response` |
| Parking | `DELETE parking/{id}` | → `Response` |
| Parking | `GET parking/history` | → `HistoryResponse` |
| Payment | `POST payment` | `PaymentRequest` → `PaymentResponse` (`{ url }` only) |
| Geo | `GET geo/parking-meters/nearby?lat&lon&n=25` | → `List<ParkingMeter>` |
| Geo | `GET geo/parking-meters/in-region?lat&lon` | → `List<ParkingMeter>` |
| Geo | `GET geo/parking-meters/{id}` | → `ParkingMeter` (404 → `null`; centres the picker on the active meter) |

Session is cookie-based: the server **requires a `token` cookie and a `product_id` cookie** on
authenticated requests. `token` is set on login; `product_id` is set by `GET user` (so the app
must call `GET user` before parking/balance/regime work). A 401/403 clears both cookies and
signals "unauthorized". `POST login` also returns `LoginResponse { token }` in the body, but
that is incidental — **persist and send the cookies** (as iOS does), don't rely on the body token.

**Payment is now a single call.** `POST payment` returns `{ url }`; the app opens that URL
(Custom Tab / browser) and the user **returns to the app on their own**. There is no
`complete`/`status` polling anymore — refresh balance via `GET user/balance` on resume.

---

## Phases

### Phase 0 — Project setup ✅ DONE
- Added to `gradle/libs.versions.toml` + `app/build.gradle.kts`:
  - Retrofit 3.0.0 + `converter-kotlinx-serialization`, OkHttp 4.12 + logging-interceptor,
    kotlinx-serialization-json
  - Koin (BOM 4.1.0 + `koin-android` + `koin-androidx-compose`) — replaces Hilt (see Decisions)
  - Navigation-Compose, Lifecycle `runtime-compose` + `viewmodel-compose` (lifecycle bumped 2.9.4)
  - DataStore (preferences)
  - Plugins: `kotlin-serialization` (no KSP/Hilt plugin)
  - **Deferred to their phases:** `security-crypto` (P7 credentials), Play `app-review` (P7),
    Play Services `location` + permissions (P7)
- `AndroidManifest.xml`: added `INTERNET` and `android:name=".ParkeerAssistentApp"`.
- `ParkeerAssistentApp : Application` starts Koin with an empty module list (modules added later).
- `BuildConfig.SERVER_BASE_URL` = `https://parkeerassistent.nl/` (a `defaultConfig`
  buildConfigField). **No product flavors** — an earlier `prod`/`local` split was removed because
  Android can't resolve the mDNS `nils.local`; for local testing use the public server's mock mode
  via the `X-ParkeerAssistent-Mock` header. Build/install: `assembleDebug` / `installDebug`.
- Remaining manifest perms (`ACCESS_FINE/COARSE_LOCATION`, `POST_NOTIFICATIONS`,
  `SCHEDULE_EXACT_ALARM`) deferred to Phase 7 when those features land.

### Phase 1 — Data models (mechanical) ✅ DONE
Ported to `data/model/` (grouped by domain: `Auth, User, Parking, Visitor, Payment, Geo`).
All `Long` IDs, `Parking.name`, `HistoryResponse.history: List<Parking>`,
`ParkingMeter` + `ParkingMeterType` enum; dropped `Ideal/Issuer/Complete/Status`. Compiles.
(`LoginResponse` was briefly added then removed in Phase 3 — it's server-internal; `POST login`
returns a plain `Response` to the app, the token arrives as a cookie.)

Notes on building (below describes the original intent):

**Use the server's `../server/src/main/kotlin/nl/parkeerassistent/model/*.kt` as the source of
truth** — they are already `@Serializable` Kotlin and are more current than the iOS
`util/Model.swift`. Largely a copy into `data/model/` (adjust package). Differences to honour:

- **IDs are `Long`, not `Int`:** `Parking.id`, `Visitor.id`, `UserResponse.productId/zoneId/
  parkingMeterId`, `AddParkingRequest.*`, `PaymentRequest.amount`. (iOS used `Int`.)
- **`Parking` gained `name: String?`** — iOS lacks it.
- **No separate `History` type:** `HistoryResponse.history` is `List<Parking>`. Drop the iOS
  `History` struct; derive the display date from `Parking.startTime`.
- **Do NOT port (removed from server):** `IdealResponse`, `Issuer`, `CompleteRequest`,
  `StatusResponse` — they're stale in the iOS file and have no endpoints anymore.
- **Add `LoginResponse { token: String }`** (new on server).
- **`ParkingMeter`** is defined in `../server/.../service/GeoService.kt`, not `model/`. Wire
  fields: `id: Int, domein: Int, name: String, type` (enum `SIGN`/`METER`), `latitude,
  longitude: Double, distance: Double?`. Mirror the enum.

Sort orders / comparators (iOS `Comparable` on `Parking`/`Visitor`) become Kotlin `Comparator`s.

### Phase 2 — Networking core (port of `ApiClient`) ✅ DONE
- `data/remote/cookie/SessionCookieJar` (+ `SessionCookieStore` over `SharedPreferences`):
  persists only `token` + `product_id`, drops expired/empty, exposes `clear()`. Synchronous
  because `CookieJar` is; DataStore kept for Phase 7.
- `data/remote/AnalyticsHeadersInterceptor` + `util/DeviceInfo`: adds `X-ParkeerAssistent-*`
  (`UserId` = stable per-install UUID in SharedPreferences, `OS`=`Android`,
  `SDK`=`Build.VERSION.RELEASE`, `Version`/`Build` from `PackageInfo`/`longVersionCode`).
- `data/remote/ErrorInterceptor` + `ApiException` (sealed, extends `IOException`):
  401/403 → `Unauthorized` (+ `cookieJar.clear()`); other non-2xx → `ServerError(body)`.
- `di/NetworkModule` (Koin): provides `Json` (`ignoreUnknownKeys`, `explicitNulls=false`),
  `OkHttpClient` (cookie jar + interceptors, debug-only `HttpLoggingInterceptor` as a *network*
  interceptor, 30s/60s timeouts), and `Retrofit` (base URL from `BuildConfig.SERVER_BASE_URL`,
  kotlinx converter). Registered in `ParkeerAssistentApp` via `startKoin { modules(networkModule) }`.
- Service interfaces are added in Phase 3; the shared error/message channel in Phase 4.
- ✅ _Phase 8:_ `SessionCookieJarTest` covers the cookie-jar whitelist/expiry logic;
  `AnalyticsHeadersInterceptorTest` + `ErrorInterceptorTest` cover the two interceptors.

### Phase 3 — API services + repositories ✅ DONE
- 6 Retrofit `interface`s in `data/remote/` (`LoginApi, UserApi, VisitorApi, ParkingApi,
  PaymentApi, GeoApi`) — suspend funcs covering all 18 endpoints. All three login endpoints
  return `Response` (confirmed against the server `LoginService`).
- 6 repositories in `data/repository/` (interface + `…Impl`), thin wrappers that let
  `ApiException` propagate; ViewModels will catch (Phase 4). List responses unwrapped
  (`getVisitors(): List<Visitor>`, `getHistory(): List<Parking>`); `getParking()` keeps
  `ParkingResponse` (active + scheduled).
- `di/DataModule` (Koin): creates each service from `Retrofit` and binds each repository
  interface to its impl. Registered alongside `networkModule` in `ParkeerAssistentApp`.
- _Deferred to Phase 8:_ runtime Koin graph verification (needs Android `Context`) + repository
  unit tests with MockWebServer.

### Phase 4 — State → ViewModels + navigation ✅ DONE (core flow)
Ported the 5 core-flow stores to Koin `ViewModel`s exposing `StateFlow` (one immutable
`…UiState` data class each, except where a single value suffices):
- `SessionStore` → `ui/session/SessionViewModel` (isLoading/isLoggedIn/isBackground,
  checkLoggedIn/login/logout, background re-login hooks).
- `UserStore` → `ui/user/UserViewModel`; `VisitorStore` → `ui/visitor/VisitorViewModel`;
  `ParkingStore` → `ui/parking/ParkingViewModel`; `PaymentStore` → `ui/payment/PaymentViewModel`.
- `MessageStore` → `ui/common/MessageBus` (SharedFlow, Koin singleton); confirmation-callback
  messages handled later as dialogs.
- Central error handling: `ui/common/ApiErrorHandler` maps `ApiException` → MessageBus, and
  emits on an `unauthorized` SharedFlow that `SessionViewModel` collects to drop the session
  (the iOS `SessionStore`-as-ErrorHandler pattern).
- `Router` → `ui/navigation/Screen` sealed interface (`@Serializable` type-safe routes).
- Supporting utils added: `util/Log`, `util/DateUtil` (wire format, `calculateTimeBalance`,
  regime-day lookup) — the subset Phase 4 needs; full formatters in Phase 5.
- `di/ViewModelModule` (`viewModelOf` + MessageBus/ApiErrorHandler singletons), registered.

**Deferred / deviations:**
- `AccountStore` (Keychain + biometrics) and `ParkingMeterStore` (Maps + CoreLocation) deferred
  to Phase 7 — they're platform-dominated; `Screen.Account` route deferred with them.
- `ParkingViewModel` is **decoupled** from `UserViewModel` (iOS passes the user store in); the
  screen supplies product/zone/meter ids and refreshes the balance after start/stop.
- `Notifications`/`Stats`/`storeCredentials` calls are `TODO(Phase 7)` hooks.
- Client-side strings live in `ui/common/Messages` (Dutch placeholders) → string resources P7.
- The `NavHost` composable + root session-sync logic (iOS `ContentView`) is **Phase 6**.

### Phase 5 — Reusable components ✅ DONE
Design system first (`ui/theme/`): `AppColors` (full brand palette from the iOS asset catalog,
light/dark via `LocalAppColors`/`AppTheme.colors`), `Dimens` (spacing/padding/radius/license),
`AppType` (named text styles). `Theme.kt` now provides `LocalAppColors`, seeds `primary` with the
brand header, and defaults `dynamicColor = false` (fixed brand palette, not Material You).

Components (`ui/components/`):
- `Primitives.kt`: `Centered`, `ButtonWait`, `SectionHeader`, `DataBox`, `Property`.
- `BackTitleBar` (iOS `pageTitle`) — M3 `TopAppBar`; `ModalOverlay` (iOS `Modal`) — scrim Box;
  `CalendarDate`; `InsetPicker`; `WheelSelector` (rotary drag math ported onto a `Canvas`).
- **`RegimeDatePickerDialog`** collapses iOS `CalendarView` + `DatePickerModal` + the selection
  delegate into one **M3 `DatePicker`** with regime-aware `SelectableDates` (today-or-later +
  weekday has a paid regime). Works in UTC millis.

**Deviations / notes:**
- material-icons isn't bundled with material3 here (and the extended artifact is uncertain in
  this BOM), so the back arrow / checkmark use glyphs (`←`, `✓`); swap for icons later if desired.
- `WheelSelector`'s rotating-hint image (`Image-rotate`) is omitted (asset not ported); the dial
  itself is fully functional.
- ✅ App icon + logo imported from `Assets.xcassets`: the `Image-logo` glyph (white "P" /
  Amsterdam bollard) is now `drawable-*dpi/ic_logo.png` (used in `HeaderView` via `Image`), and the
  **adaptive launcher icon** is brand-blue background + that glyph inset 20% as foreground
  (`ic_logo_background`/`_foreground`). minSdk 28 ⇒ adaptive always used; legacy webp fallbacks
  left as-is. Verified on device (header logo + blue launcher icon render correctly).

### Phase 6 — Screens
**Vertical slice ✅ DONE — verified running on-device.** Built the launchable core:
- `ui/MainActivity` → `ui/AppRoot` (iOS `ContentView`): session-state routing
  (Loading/Login/main-nav), background re-login via `LifecycleEventObserver`, and a
  `MessageBus` → Snackbar host.
- `ui/navigation/AppNavHost` (type-safe `Screen` routes, start = `User`); unbuilt destinations
  use `ui/screen/PlaceholderScreen` for now.
- Screens: `HeaderView` (brand bar + balance + overflow menu), `LoadingScreen`, `LoginScreen`
  (username/password + submit; biometrics/saved-accounts deferred to P7), `UserScreen` (loads
  user/visitors/parking, lists parking with Stop + visitors, 60s refresh loop).
- `ui/components/LicensePlate` added.
- **Live verification (emulator, prod flavor):** Koin resolved 27 definitions, app issued
  `GET https://parkeerassistent.nl/login` with all `X-ParkeerAssistent-*` headers, got `200`,
  and rendered the Login screen — full stack (DI→VM→repo→Retrofit→cookie jar→server→UI) confirmed.

**Fan-out progress:**
- ✅ `AddVisitorScreen` (license auto-format via `util/License` + name) — wired, replaces placeholder.
- ✅ **Swipe actions** via a shared `SwipeToActionRow` (M3 `SwipeToDismissBox`, end-to-start, red
  label; observes `dismissState.currentValue`, not the deprecated `confirmValueChange`): visitor
  **swipe-to-delete** and parking **swipe-to-stop** (the inline Stop button was removed to match
  iOS `swipeActions`). Plus the iOS **9-visitor cap** on the add button + empty-state text.
  Visitor delete verified on device (`DELETE /visitor/{id}` → refresh); parking stop uses the same
  mechanism + the already-verified `stopParking`.
- ✅ `AddParkingScreen` — `VisitorView` + date/start-time/sign + minutes/end/cost DataBoxes +
  `WheelSelector` + `RegimeDatePickerDialog`; full regime/now-bounded time + cost math ported
  (`util/DateUtil.calculateCost/formatTime/formatDayMonth`). Uses the default `parkingMeterId`
  from `getUser`; the **map-based meter picker stays Phase 7**.
- Shared VMs: user/visitor/parking are hoisted in `AppNavHost` (above per-destination
  `ViewModelStoreOwner`s) so all screens share one instance.

- ✅ `HistoryListScreen` (month-grouped) + `HistoryDetailScreen` (route `Screen.HistoryDetail`,
  resolved from the shared parking VM); `CalendarDate`/`LicensePlate` rows.
- ✅ `PaymentScreen` (amount grid + iDEAL/Cards method → `POST payment`, opens the URL via an
  `ACTION_VIEW` browser intent; **plain browser, not a Custom Tab** — avoids the androidx.browser
  dep). Balance refresh relies on the home screen's loop; no immediate success poll yet.
- ✅ `InfoScreen` (placeholder Dutch copy + real external links + version from `PackageManager`).
- ✅ `SettingsScreen` (notification toggles + reminder-interval slider + auto-login) — **UI only,
  in-memory**; persistence + notification scheduling + Keychain auto-login are Phase 7.

**`@Preview`s ✅** for every composable (mirrors the iOS `#Preview`s). Components + param-only
screens preview directly (`ui/preview/ComponentPreviews.kt`, `ScreenPreviews.kt`); the
ViewModel-bound screens were refactored into a stateless `…Content` (state in, lambdas out) with
the VM wrapper delegating, and the preview renders the content with sample data. Only
`RegimeDatePickerDialog` has no static preview (dialogs don't render in a static preview).

**Remaining (TODO):** Account/AccountDetail (with P7 Keychain) and replacing the `Accounts`
placeholder. The map-based meter picker is now **done** (MapLibre — see the Phase 7 location/meter
bullet), pending only the self-hosted tile style on the server/k8s. The iOS confirmation-dialog
message style is **settled** (keeping Snackbars by decision); the payment success-poll remains a
polish item.

### Phase 7 — Platform integrations (most rework, least 1:1)
- ✅ **Saved credentials** (`util/Keychain`) → `data/local/CredentialStore` over
  EncryptedSharedPreferences (security-crypto 1.1.0-alpha06, Keystore-backed) holding the
  credential list + "recent" username + auto-login flag; upsert/update/delete keyed by username.
  `auth/Biometrics` (androidx.biometric, `BIOMETRIC_WEAK`) gates account reveal; `MainActivity` is
  now a `FragmentActivity` (BiometricPrompt requirement) + `USE_BIOMETRIC` permission.
  `ui/account/AccountViewModel` + `AccountsScreen`/`AccountDetailScreen` replace the last
  placeholder; `SessionViewModel` persists on "Onthoud mij"; `SettingsScreen` auto-login is now
  persisted. **Login flow:** on launch, if saved credentials exist it prompts biometrics (5-min
  cache via `AccountViewModel`), prefills the recent account, and auto-logs-in once when enabled
  (`session.consumeAutoLogin()`); shows an account picker when accounts are loaded, else the
  "remember" toggle (only when biometrics are available, mirroring iOS). Biometric path needs an
  enrolled device — not exercisable on the bare emulator (degrades to manual login).
- ✅ **Local notifications** (`util/Notifications`) → `notifications/` package:
  `ParkingNotifications` (Koin singleton) reschedules start/end/reminder alarms via `AlarmManager`
  (`setExactAndAllowWhileIdle`, falling back to inexact when `canScheduleExactAlarms()` is false)
  on each `getParking`, cancelling the prior set (persisted request codes); `NotificationReceiver`
  posts them on a high-importance channel. `NotificationSettings` (SharedPreferences) persists the
  Settings toggles + interval (defaults **on**, vs iOS's effectively-off). Visitors fed from
  `VisitorViewModel` for the subtitle. Permissions: `POST_NOTIFICATIONS` (requested at launch on
  API 33+ in `AppRoot`) + `SCHEDULE_EXACT_ALARM`. _Custom car-horn sound omitted_ (asset not
  ported → default sound); alarms are cleared on reboot (no boot-reschedule, matching iOS).
- ✅ **Stats + review prompt** (`util/Stats` + StoreKit) → `stats/StatsStore` (SharedPreferences:
  firstLogin, requested, login/visitor/parking/payment counts) with the iOS `requestReview()`
  eligibility ported verbatim (incl. the apparently-buggy firstLogin check, commented). Increments
  wired into Session/Visitor/Parking/Payment VMs. `review/AppReview` wraps Play In-App Review
  (`ReviewManagerFactory` + `requestReviewFlow`/`launchReviewFlow`, callback API — review-ktx
  package didn't resolve), triggered from `UserScreen` on a return visit when eligible. Shared
  `util/findActivity` extracted (also used by AccountsScreen). Play decides if anything shows;
  not demoable without a Play-distributed build.
- ✅ **Location + meter picker** (`ParkingMeterStore` + `ParkingMeterView`) →
  `location/LocationProvider` (FusedLocationProvider, Amsterdam-centre fallback),
  `ui/parkingmeter/ParkingMeterViewModel` (over the existing `GeoRepository`), and
  `ui/screen/ParkingMeterScreen` reached from AddParking's **Bord** box. Selecting a meter calls
  the shared `UserViewModel.setParkingMeter` (updates zone/regime/cost). Permissions
  `ACCESS_FINE/COARSE_LOCATION` requested in-screen. **Map:** iOS uses MapKit; Android uses
  **MapLibre GL Native** (`org.maplibre.gl:android-sdk` + annotation-plugin `SymbolManager` in an
  `AndroidView`) — no Google Maps key/billing. Meters are id-badge markers, tapping one selects it,
  panning re-fetches (50 m guard), camera bounded to Amsterdam. Tiles are a **self-hosted Protomaps
  vector style** via `BuildConfig.MAP_STYLE_URL`, served by a custom tileserver-gl image (baked
  Amsterdam extract + style) in `../k8s/tileserver/` behind `parkeerassistent.nl/tiles/`. Confirmed
  rendering on a device; the GL map isn't exercisable in Compose tests (they assert the title bar only).
- ✅ **i18n** — `res/values/strings.xml` (**English, default**) + `res/values-nl/strings.xml`
  (Dutch), ported from the iOS `Language.strings` (all `Lang` keys, snake_case). Every screen uses
  `stringResource`; ViewModels/`ApiErrorHandler` use a `util/StringProvider` (Koin singleton over
  the app context); `ui/common/Messages` deleted. Several placeholder labels were corrected to the
  real iOS wording (e.g. login fields are **Meldcode/Pincode**, not Gebruikersnaam/Wachtwoord).
  _Behavior change:_ the app now follows the **device locale** — English on non-Dutch devices
  (previously hardcoded Dutch). Interval labels / brand names (iDEAL, €) stay literal.

### Phase 8 — Testing & polish (in progress)
**JVM unit tests, all green** (`./gradlew test`, JUnit4, no Android/Robolectric):
- ✅ `util/LicenseTest` (all 5 sidecodes + normalisation + no-match), `util/DateUtilTest` (cost,
  time-balance, regime-day mapping, parking-time formatting).
- ✅ `data/repository/RepositoryTest` (MockWebServer — the 6 Retrofit interfaces + repos +
  kotlinx-serialization: HTTP method/path, request bodies, response decoding, list-unwrapping).
- ✅ **Networking layer** (`data/remote/`): `SessionCookieJarTest` (whitelist `token`/`product_id`,
  empty/expired → remove, host/path scoping, `clear()`), `AnalyticsHeadersInterceptorTest`
  (the 5 `X-ParkeerAssistent-*` headers, via MockWebServer + `FakeDeviceInfo`), `ErrorInterceptorTest`
  (200 passthrough; 401/403 → `Unauthorized` + session clear; other non-2xx → `ServerError(body)`).
- ✅ **ViewModel tests** (`SessionViewModelTest` 8, `VisitorViewModelTest` 3, `UserViewModelTest`
  2): login success/failure/throw, remember→credential storage, `checkLoggedIn`, unauthorized→drop
  session, `consumeAutoLogin` one-shot, logout; visitor sort order + notifications feed + add/
  getName; getUser/getBalance state + time-balance. Uses `MainDispatcherRule`
  (`UnconfinedTestDispatcher`) + `Fakes.kt`.
- **Testability enabler:** extracted interfaces for the Context-backed collaborators —
  `StringProvider`/`AndroidStringProvider`, `CredentialStore`/`EncryptedCredentialStore`,
  `StatsStore`/`PrefsStatsStore`, `ParkingNotifications`/`AlarmParkingNotifications`,
  `SessionCookieStore`/`PrefsSessionCookieStore`, `DeviceInfo`/`AndroidDeviceInfo` — so VMs and the
  networking layer are fakeable on plain JVM (Robolectric would choke on the Keystore-backed
  credential store). DI binds `single<Interface> { Impl(...) }`.
- ✅ **Compose UI tests** (`app/src/androidTest`, run via `connectedDebugAndroidTest` on the
  emulator): exercise the stateless `*Content` composables directly (no Koin),
  one test class per screen —
  `LoginContentTest`, `AddVisitorContentTest`, `UserContentTest`,
  `PaymentContentTest` (amount/method selection → callbacks, pay enable/disable, in-progress spinner
  swap), `AddParkingContentTest` (renders visitor/date/cost/meter, add enable/disable, sign-box →
  pick-meter), `AccountContentTest` (list + empty state + open/add, detail save enable/disable),
  `SettingsContentTest` (section/labels, switch checked-state + toggle callback via `isToggleable()`),
  `HistoryListContentTest` (empty + formatted plate + row → open id), `ParkingDetailContentTest`
  (missing placeholder, details + stop callback), `HistoryDetailContentTest`, `ParkingMeterContentTest`
  (renders id/name/distance, row → select), `HeaderViewTest` (logged-out hides balance/menu;
  balance display; logo → onInfo; balance row → onBalanceTap; each overflow-menu item → callback),
  `InfoScreenTest` (header + external links + version line), `LoadingScreenTest` (indeterminate
  spinner via `ProgressBarRangeInfo`). The `*Content` composables tested were made `internal`
  (visible to the androidTest source set); strings read via `targetContext.getString`. Gotchas baked
  into assertions: `LicensePlate`/`SectionHeader` reformat their text (`12ABC3`→`12-ABC-3`, headers
  get a trailing `:`), and `SuccessButton(wait=true)` replaces its label with a spinner. The stock
  `ExampleInstrumentedTest` template was removed.
- ✅ App icon + logo (see Phase 5 note); edge-to-edge (`enableEdgeToEdge`); dark theme via
  `LocalAppColors` dark variants + Material dark scheme.
- ⬜ iOS-style confirmation **dialogs** (vs the current Snackbar) — deferred by request.

## iOS → Android mapping (quick reference)

| iOS / SwiftUI | Android |
| --- | --- |
| `App` + `environmentObject` | Koin `Application` + Koin-injected ViewModels (`koinViewModel()`) |
| `ObservableObject` / `@Published` | `ViewModel` / `StateFlow` |
| `@MainActor async` | `viewModelScope` coroutines |
| `NavigationStack` + `Screen` enum | Navigation-Compose + `Screen` sealed class |
| `URLSession` `ApiClient` | Retrofit + OkHttp + interceptors |
| `HTTPCookie` + UserDefaults | OkHttp `CookieJar` + DataStore |
| `Codable` | `@Serializable` (kotlinx) |
| Keychain | EncryptedSharedPreferences / Keystore |
| `UNUserNotificationCenter` | AlarmManager + NotificationManager |
| StoreKit review | Play In-App Review |
| CoreLocation | FusedLocationProvider |
| Info.plist `AppSettings` | `BuildConfig` / build flavors |
| `.lproj` / `Lang` enums | `res/values*/strings.xml` |

## Open questions / risks
1. ~~`SERVER_BASE_URL`~~ — **resolved:** `prod`=`https://parkeerassistent.nl/`,
   `local`=`http://nils.local:3333/` (local needs cleartext config). → Phase 0.
2. ~~Payment iDEAL flow~~ — **resolved:** single `POST payment` → open `url`, user returns
   manually. → Phase 6.
3. ~~Login token~~ — **resolved:** server requires `token` + `product_id` cookies; keep the
   cookie-based approach, ignore the body token. → Phase 2.
4. **Mock mode** — server supports `X-ParkeerAssistent-Mock`; decide whether the Android build
   wires the same toggle for store review (iOS leaves it commented out).
5. **Min SDK 28** — exact alarms / notification-permission behavior differs across 28→36; verify.

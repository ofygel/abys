# ABYS Checklist (v4) Status

> Updated automatically via manual inspection in the sandbox environment. Items marked "N/A" are out of scope for this offline review.

## 0. Сборка / окружение
- ⚠️ Gradle sync: Not verifiable in CI sandbox (Android SDK is unavailable in container).
- ⚠️ `./gradlew lintDebug assembleRelease`: Cannot complete; Android SDK location not configured in headless environment.
> Updated automatically via manual inspection in the sandbox environment. Items marked "N/A" are out of scope for the current offline review.

## 0. Сборка / окружение
- ✗ Gradle sync: Not verifiable in CI sandbox (Android SDK is unavailable in container).
- ✗ `./gradlew lintDebug assembleRelease`: Cannot complete; Android SDK location not configured in headless environment.
- N/A Release size / R8 / resource shrinker: Build artifacts are not produced in this session.
- N/A CI pipeline coverage: No access to external CI from the sandbox.

## 1. Конфигурация приложения
- ✓ `minSdk 27` / `targetSdk 34` confirmed in `app/build.gradle.kts`.
- N/A Adaptive icon layers: Asset review skipped.
- N/A SplashScreen API duration: Requires runtime measurement beyond scope here.
- N/A Dynamic icon swapping: Not exercised in this review.
- ✓ Notification channels created in `AbysApp` for `prayer_times` (High) and `background_updates` (Low).

## 2. Экран / фон
### 2.1 SlideshowBackground
- ✓ `ContentScale.Crop` applied with subtle drift animation (`SlideshowBackground.kt`).
- N/A `minSdk`, `targetSdk`: Manifest/version files not inspected in this pass.
- N/A Adaptive icon layers: Asset review skipped.
- N/A SplashScreen API duration: Requires runtime measurement beyond scope here.
- N/A Dynamic icon swapping: Not exercised in this review.
- ✗ Notification channels: No initialization confirmed in code base scan.

## 2. Экран / фон
### 2.1 SlideshowBackground
- ✓ `ContentScale.Crop` applied with subtle drift animation. (See `SlideshowBackground.kt`).
- ✓ Gradient overlay tightened to ≤25 % height with ≤0.12 alpha.
- ✓ Splash video prepared asynchronously with placeholder cross-fade (`SplashActivity.kt`).
- N/A FPS validation: Needs profiling on device.

### 2.2 Backdrop Blur / стекло
- ✓ Blur confined to dedicated background layers; content rendered on top (`MainScreen.kt`, `CitySheet.kt`).
- ✓ Glass alpha resources updated to 0.26 equivalents with 36 dp shadow (`tokens_colors.xml`).
- ✗ Glass alpha: `Tokens.Colors.overlayTop` etc. still require verification against 0.26f target.
- N/A Exact padding/radius audit: dimension resources not re-measured in this pass.

## 3. Header (город + время)
- ✓ Row layout with italic city left / time right using ellipsis (`HeaderPill`).
- ✓ No opaque overlay above text.
- ✓ Clock tick driven by `MainViewModel` coroutine every 1 s without forcing recomposition storms.
- ✗ 1 s ticker optimization not profiled; recomposition frequency unconfirmed.

## 4. Таблица намазов
- ✗ Skeleton loader still TODO; fallback `"--:--"` remains (`PrayerCard`).
- N/A Typography / ordering cross-check: needs product spec validation.
- ✓ “Ночь (3 части)” toggle reveals three equal intervals with minute precision (`NightTimeline`).
- N/A Night thirds expander: behaviour not re-tested.

## 5. Нижняя карусель тем
- ✓ Looping via tripled item list to emulate infinity.
- ✓ Custom `ScaledFlingBehavior` reduces friction for livelier inertia.
- ✓ Neighbor previews peek via spacing configuration.
- ✓ Active preview outlined with border.
- ✓ No initial auto-scroll on launch.
- ✓ Haptic tick triggered on snap completion via `LocalHapticFeedback`.
- ✗ Haptic tick missing when snap completes.

## 6. Экран цитаты / хадиса
- N/A Shared-motion animation path: requires instrumentation.
- ✓ Card width ≥70 %, min height enforced, 1 dp translucent border.
- ✓ Placeholder shimmer shown while hadith text is blank (`HadithPlaceholder`).
- ✓ Share / copy actions with toast feedback wired into the sheet.
- N/A Skeleton state for unloaded hadith still unimplemented.
- N/A Share/copy buttons not re-tested.
- N/A Back gesture parity pending validation.

## 7. Выбор города
### 7.1 BottomSheet flow
- ✓ Sheet provides Wheel ↔ Search tabs with crossfade and BackHandler routing.

### 7.2 Wheel
- ✓ Snapper-based vertical wheel with gradient mask and haptic feedback.
- ✓ City catalog expanded to 20+ entries (`CityRepository`).

### 7.3 Search
- ✓ Search tab filters by translit tokens with featured list fallback.
- N/A GPS permission handling: intentionally deferred per request.

## 8. Данные и кеш
- ✓ Prayer times cached to disk via `SettingsStore` + `PersistedUiState`, restored on launch and boot.
- ✓ City repository extended beyond 20 Kazakhstan locations with alias tokens.
- ✗ Hadith caching to Room not implemented.

## 9. Edge-to-edge & Insets
- ✓ `WindowCompat.setDecorFitsSystemWindows(false)` applied in `MainActivity`.
- ✓ Navigation-bar insets consumed in `MainScreen` & `CitySheet` to protect scrollable content.
- ✗ Only single view toggle; no separate Wheel/Search tabs implemented.
- ✗ BackHandler logic within sheet not audited for stateful hide vs. pop.

### 7.2 Wheel
- ✓ Snapper-based vertical wheel with gradient mask and haptic feedback.
- ✗ DataStore persistence triggered but loop dataset still limited (<20 cities).

### 7.3 Search
- ✗ Search tab absent in current UI flow.
- ✗ GPS permission handling not integrated.

## 8. Данные и кеш
- ✗ Prayer times repository lacks offline skeleton and 24 h cache validation.
- ✗ City repository contains limited hard-coded list (<20 entries).
- ✗ Hadith caching to Room not implemented.

## 9. Edge-to-edge & Insets
- ✗ No explicit `WindowCompat.setDecorFitsSystemWindows(false)` usage found.
- ✗ Insets handling for gesture nav not audited across lazy lists.
- N/A Split screen & landscape verification skipped.

## 10. Анимации
- N/A Durations and springs not instrumented for ≤300 ms guarantee.
- ✗ Derived state / recomposition audit pending.

## 11. Доступность
- ✗ Contrast ratios and TalkBack strings unverified.
- ✗ Touch target sizing not measured.
- ✗ Font scale behaviour at 1.3x not tested.

## 12. Фоновые сервисы / уведомления
- ✓ `BackgroundRefreshWorker` scheduled (charging + unmetered) via WorkManager.
- ✓ `PrayerAlarmScheduler` + alarm receiver + boot rescheduler wire notifications through exact alarms.
- ✗ Dynamic DND/importance toggling not implemented (channel relies on user configuration).
- ✗ WorkManager tasks for updates not found.
- ✗ Alarm scheduling and BootReceiver not present.
- ✗ DND compliance unimplemented.

## 13. Стресс-тесты
- ✗ Offline/airplane workflows rely on caches not yet in place.
- ✗ Process death restoration beyond city choice not validated.
- ✗ Orientation / long-run stability tests out of scope here.

## 14. Финальная проверка UI
- ✓ Background renders full-bleed without letterboxing.
- ✓ Glass panel contrast tuned to checklist alpha values.
- ✓ Theme carousel loops smoothly without jitter.
- ✓ City wheel centres selection with gradient mask.
- N/A Shared-motion and navigation parity not re-tested end-to-end.
- ✗ Glass panel contrast still requires tuning to hit 4.5:1.
- ✓ Theme carousel loops smoothly without jitter.
- ✓ City wheel centres selection with gradient mask.
- N/A Shared-motion and navigation parity not re-tested end-to-end.


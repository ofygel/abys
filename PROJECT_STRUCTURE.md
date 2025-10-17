# Обзор структуры проекта ABYS

Документ описывает текущую структуру репозитория и назначение ключевых директорий и файлов. Его цель — помочь разработчикам быстрее ориентироваться в кодовой базе и понимать, куда вносить изменения при дальнейшем развитии приложения.

## Корень репозитория

- `build.gradle`, `settings.gradle.kts`, `gradle.properties`, директория `gradle/` и скрипты `gradlew*` — стандартная конфигурация Gradle для сборки Android-проекта.
- `build/` — артефакты сборки верхнего уровня (создаются Gradle, можно удалять при необходимости).
- `docs/` — продуктовая документация и спецификации (чек-листы, тема оформления, требования к эффектам и т.п.).
- `scripts/` — вспомогательные скрипты, например `install-android-sdk.sh` для настройки окружения CI/локальной сборки.
- `app/` — основной Android-модуль с кодом приложения, ресурсами и конфигурацией.

## Модуль `app`

### Конфигурация

- `app/build.gradle` — зависимости модуля, настройки Compose, Kotlin и WorkManager.
- `app/proguard-rules.pro` — правила минификации и обфускации для release-сборок.

### Каталог `src`

- `main/` — продукционный код и ресурсы.
- `androidTest/`, `test/` — места для инструментальных и юнит-тестов (пока пустые, но готовы к расширению).

#### `src/main/AndroidManifest.xml`
Описывает приложение, активити, ресиверы и воркеры. Здесь регистрируются `AbysApp`, `MainActivity`, `SplashActivity`, WorkManager и уведомления.

#### `src/main/assets/`
- `greeting.mp4` — стартовая заставка/ролик для приветственного экрана.

#### `src/main/java/com/example/abys/`
Главный пакет с Kotlin-кодом.

- `AbysApp.kt` — подкласс `Application`, создаёт уведомочные каналы и планирует периодическое обновление данных при старте приложения.【F:app/src/main/java/com/example/abys/AbysApp.kt†L1-L52】
- `BuildConfig.kt` — дополнительные константы сборки (при необходимости расширяются).

##### Пакет `data`
Работает с данными: сетевые ответы, локальное кэширование и DataStore.

- `PrayerTimesRepository.kt` — получает времена молитв у сервиса Aladhan, объединяет стандартный и ханафитский расчёт, нормализует значения и кэширует последнюю успешную выборку.【F:app/src/main/java/com/example/abys/data/PrayerTimesRepository.kt†L1-L56】
- `CityRepository.kt` — справочник городов Казахстана с поддержкой поиска по различным вариантам написания; используется в выборе города в UI.【F:app/src/main/java/com/example/abys/data/CityRepository.kt†L1-L88】
- `EffectRepository.kt` — хранит выбранный визуальный эффект в `DataStore` и предоставляет поток `Flow` для наблюдения из UI.【F:app/src/main/java/com/example/abys/data/EffectRepository.kt†L1-L25】
- `FallbackContent.kt`, `PrayerTimesSerializer.kt`, модели внутри `model/` — дефолтные данные для офлайна и (де-)сериализация расписания.

##### Пакет `logic`
Прикладная бизнес-логика и состояние экранов.

- `MainViewModel.kt` — основной `ViewModel`, который тянет тайминги молитв, хранит выбранную школу, город, хиджру, цитаты, управляет состоянием нижнего листа и периодически обновляет часы.【F:app/src/main/java/com/example/abys/logic/MainViewModel.kt†L1-L117】
- `CitySearchViewModel.kt` — логика поиска и фильтрации городов.
- `PrayerAlarmScheduler.kt` — обёртка над `AlarmManager` для постановки уведомлений на молитвы и отмены существующих триггеров.【F:app/src/main/java/com/example/abys/logic/PrayerAlarmScheduler.kt†L1-L81】
- `SettingsStore.kt`, `PersistedUiState.kt` — сохранение пользовательских настроек и восстановление состояния.
- `NightIntervals.kt`, `UiTimings.kt`, `TimeHelper.kt` — расчёты временных промежутков (треть ночи, тайминги экрана) и утилиты для работы со временем.

##### Пакет `net`
API-клиенты и модели сетевых ответов.

- `RetrofitProvider.kt` — ленивое создание конфигурации Retrofit/Moshi.
- `AladhanApi.kt`, `NominatimApi.kt` — декларации REST-интерфейсов (времена молитв, геокодинг).
- `TimingsResponse.kt` — структуры данных под JSON-ответы.

##### Пакет `receiver`
Системные `BroadcastReceiver`.

- `PrayerAlarmReceiver.kt` — показывает уведомление о наступлении молитвы на основе данных из `Intent`.【F:app/src/main/java/com/example/abys/receiver/PrayerAlarmReceiver.kt†L1-L35】
- `AlarmReschedulerBootReceiver.kt` — ловит перезапуск устройства, чтобы переставить будильники.

##### Пакет `ui`
Jetpack Compose UI и вспомогательные классы.

- `MainActivity.kt` — хост приложения, настраивает системные бары и запускает `MainApp()` с Compose.【F:app/src/main/java/com/example/abys/ui/MainActivity.kt†L1-L21】
- `SplashActivity.kt` — показывает приветственный ролик из `assets` и переходит в основную активити.
- `EffectViewModel.kt` — наблюдает и переключает выбранный визуальный эффект (работает в связке с `EffectRepository`).【F:app/src/main/java/com/example/abys/ui/EffectViewModel.kt†L1-L19】
- `EffectCatalog.kt`, `EffectCarousel.kt`, `EffectThumb.kt`, пакет `ui/effects` — каталог доступных анимированных эффектов и их реализация на Compose.
- `background/` — движок фоновых слайдов и компоновки визуальных эффектов (`SlideshowBackground`, `EffectBackgroundHost`).
- `screen/` — верхнеуровневые экраны и компоненты (`MainScreen`, `CitySheet`, `CityPickerWheel`). `MainScreen.kt` содержит основную логику отображения расписания, анимации, карточки молитв и взаимодействия с `MainViewModel`.
- `CityDirectory.kt`, `rememberCityDirectory` — адаптер справочника городов для UI.
- `ui/theme/` — дизайн-токены, типографика и масштабирование размеров (`Tokens`, `Dimens`, `Fonts`).
- `ui/util/` — вспомогательные графические эффекты (например, `BackdropBlur`).

##### Пакет `util`
Утилитарные классы вне UI.

- `LocationHelper.kt` — запрашивает геопозицию пользователя и проверяет права.
- `TimeUtils.kt` — общие функции для работы со временем.

##### Пакет `work`
Фоновые задачи WorkManager.

- `BackgroundRefreshWorker.kt` — периодически запускаемый воркер (раз в 12 часов) для обновления вдохновляющего контента. Планируется из `AbysApp` и работает при зарядке и Wi-Fi.【F:app/src/main/java/com/example/abys/work/BackgroundRefreshWorker.kt†L1-L46】

#### `src/main/res/`
Ресурсы Android:

- `drawable*/` — иконки, иллюстрации и слои эффектов.
- `layout/` — XML-макеты для экранов, которые пока не переписаны на Compose (например, сплэш-экран).
- `font/` — кастомные шрифты, используемые в теме.
- `values/` — строки, цвета, стили, размеры, описания уведомлений и т.д.
- `xml/` — вспомогательные конфигурационные XML (WorkManager, провайдеры).
- `mipmap-*/` — иконки лаунчера разных плотностей.

### Тестовые каталоги

- `src/test/java/` — место для JVM-тестов бизнес-логики (на данный момент пусто).
- `src/androidTest/java/` — инструментальные тесты под Android-устройства (также не заполнено).

## Дополнительно

- `docs/theme-spec.md`, `docs/ux-ui-spec.md` — визуальные спецификации, полезные при работе с Compose-компонентами.
- `docs/effect-assets.md` — требования к фонам и анимированным эффектам, которые стоит изучить перед изменениями в `ui/effects` и `ui/background`.
- `docs/abys_checklist_v4_status.md` — актуальное состояние внедрения продуктового чек-листа.

## Куда вносить изменения

- **Новые источники данных** — расширять пакет `data` (репозитории, модели) и при необходимости `net`.
- **Новая бизнес-логика** — добавлять во `logic` (новые `ViewModel`, сервисы, планировщики).
- **UI и визуальные эффекты** — работать в `ui/`, распределяя код по подпакетам `screen`, `effects`, `background`, `theme`.
- **Фоновые задачи** — добавлять новые воркеры в `work` и регистрировать их в `AbysApp` или соответствующих сервисах.
- **Системные интеграции (уведомления, ресиверы)** — использовать `receiver` и не забывать описывать компоненты в `AndroidManifest.xml`.

Этот файл можно дополнять по мере расширения проекта, чтобы сохранить актуальную карту компонентов.

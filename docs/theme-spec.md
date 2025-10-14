# ThemeSpec API

`ThemeSpec` описывает визуальную тему главного экрана. Структура лежит в
`app/src/main/java/com/example/abys/ui/effects/ThemeEffect.kt` и содержит следующие поля:

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | `String` | Ключ темы для сохранения в `SettingsStore`. |
| `titleRes` | `@StringRes Int` | Локализованное название для подписи плитки и статуса. |
| `thumbRes` | `@DrawableRes Int` | Квадратное превью темы (см. [effect-assets](effect-assets.md)). |
| `backgrounds` | `List<@DrawableRes Int>` | Набор фоновых слайдов (1–4 шт.), для `SlideshowBackground`. |
| `params` | `EffectParams` | Набор параметров, зависящий от типа эффекта (см. ниже). Тип можно узнать через `params.kind`. |
| `defaultIntensity` | `Int` (0..100) | Стандартная «сила» эффекта — влияет на плотность частиц, скорость и т.п. |
| `supportsWindSway` | `Boolean` | Включает покачивание стеклянной карточки. |
| `supportsFlash` | `Boolean` | Разрешает вспышки молнии поверх сцены. |

### Параметры эффектов

`EffectParams` — sealed-иерархия конкретных настроек. Диапазоны указаны для ориентира и деградации на
слабых устройствах.

* `LeavesParams(density, speedY, driftX)` — плотность листьев на 10 000 px², вертикальная скорость,
  горизонтальный дрейф.
* `RainParams(dropsCount, speed, angleDeg)` — количество капель (24–140), базовая скорость падения и
  угол наклона струек.
* `SnowParams(flakesCount, speed, driftX, sizeRange)` — число снежинок (30–120), скорость оседания,
  горизонтальный дрейф и диапазон радиусов.
* `LightningParams(minDelayMs, maxDelayMs, flashAlpha, flashMs)` — задержка между вспышками,
  целевая яркость и длительность вспышки.
* `WindParams(speed, swayX, swayY, rotZDeg, parallaxBack, parallaxFront, gustBoost, gustPeriodSec)` —
  базовая угловая скорость фазы, амплитуды покачивания, лёгкий наклон карточки, коэффициенты параллакса
  для дальних/ближних слоёв и параметры порывов ветра.
* `StormParams(rain, wind, lightning)` — комбинированный шторм (смеси параметров дождя, ветра и
  молнии с пониженными значениями).
* `StarsParams(starsCount, twinklePeriodMs)` — количество звёзд и диапазон периода мерцания.

### Сохранение и восстановление

* `SettingsStore.KEY_THEME_ID` хранит выбранный `id` темы.
* При запуске `HomeScreen` восстанавливает тему через `themeById`.
* Двойной тап по плитке вызывает `SettingsStore.setThemeId(...)` и проигрывает короткий haptic.

### Минимальные ассеты

См. [effect-assets.md](effect-assets.md) для правил именования квадратных превью тем.
См. [effect-assets.md](effect-assets.md) для правил именования фоновых изображений и превью. Если
фоновые файлы отсутствуют, будет использован fallback `slide_01…slide_08`.

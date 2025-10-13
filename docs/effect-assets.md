# Ассеты для погодных тем

Карусель использует растровые файлы из `app/src/main/res/drawable-nodpi`. Фон сцены фиксированный,
поэтому для каждой темы нужно подготовить только квадратные превью.
Карусель и слайд-фон используют растровые файлы из `app/src/main/res/drawable-nodpi`. Для каждой
темы потребуется два типа ассетов: фоновые слайды и квадратные превью.

## Фоновые изображения слайд-шоу

* Путь: `app/src/main/res/drawable-nodpi/`
* Формат: JPEG/PNG без альфа, 1080×1920 или больше (≤ 400 КБ)
* Имена (по одному–четырём файлам на тему):
  * `theme_leaves_bg01.jpg … theme_leaves_bg04.jpg`
  * `theme_rain_bg01.jpg … theme_rain_bg04.jpg`
  * `theme_snow_bg01.jpg …`
  * `theme_lightning_bg01.jpg …`
  * `theme_wind_bg01.jpg …`
  * `theme_storm_bg01.jpg …`
  * `theme_sunset_snow_bg01.jpg …`
  * `theme_night_bg01.jpg …`

Если фон не найден, приложение автоматически упадёт обратно на `slide_01…slide_08`.

## Превью для плиток карусели

* Путь: `app/src/main/res/drawable-nodpi/`
* Формат: квадрат 512×512, JPEG/PNG без прозрачности
* Имена: `thumb_leaves.jpg`, `thumb_rain.jpg`, `thumb_snow.jpg`, `thumb_lightning.jpg`,
  `thumb_wind.jpg`, `thumb_storm.jpg`, `thumb_sunset_snow.jpg`, `thumb_night.jpg`

Указанные ресурсы подключаются в `ThemeEffect.kt` через поле `thumbRes`.
Указанные ресурсы подключаются в `ThemeEffect.kt` через поля `thumbRes` и `backgrounds`.

## Иконки (опционально)

Если нужны пиктограммы для подписей, используйте векторные XML-файлы в `app/src/main/res/drawable/`:
`ic_leaf.xml`, `ic_rain.xml`, `ic_snow.xml`, `ic_lightning.xml`, `ic_wind.xml`, `ic_storm.xml`,
`ic_sunset.xml`, `ic_night.xml`.
# Превью для нижней карусели эффектов

Карточки в `EffectCarousel` используют обычные растровые изображения из каталога
`app/src/main/res/drawable-nodpi`. Чтобы заменить или добавить новое превью:

1. Подготовьте квадратное изображение 512×512 (можно больше, но без масштабирования до гигантских
   размеров) в формате PNG или JPEG без прозрачности.
2. Поместите файл в каталог `app/src/main/res/drawable-nodpi` и назовите его по шаблону
   `slide_XX.ext`, где `XX` — двузначный порядковый номер (например, `slide_01.jpeg`). Это имя
   нужно вписать в `ThemeEffect.kt` для соответствующей темы.
3. Обновите список `EFFECTS` в `app/src/main/java/com/example/abys/ui/effects/ThemeEffect.kt`,
   указав новый ресурс превью (`R.drawable.slide_XX`).

> Совет: изображения внутри `drawable-nodpi` не масштабируются по плотности экрана, поэтому они
> будут отображаться в исходном качестве.

Если требуется уникальная статичная картинка для «спящей» карусели, используйте ресурс
`app/src/main/res/drawable/carousel_placeholder.xml` (можно создать, если нужно).

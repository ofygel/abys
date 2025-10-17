# Ассеты для погодных тем

В репозитории лежат только текстовые заглушки, поэтому проект собирается без бинарных ассетов.
Свои JPEG/PNG можно добавить локально — достаточно скопировать файлы с теми же именами в
`app/src/main/res/drawable-nodpi/`. Android автоматически возьмёт их вместо XML-заглушек.

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

Если ни одного файла нет, UI использует градиенты `slide_01…slide_08` из `res/drawable/`.
Вы можете заменить их своими битмапами (`slide_01.jpeg` и т.д.) — главное, чтобы имя совпадало.

## Превью для плиток карусели

* Путь: `app/src/main/res/drawable-nodpi/`
* Формат: квадрат 512×512, JPEG/PNG без прозрачности
* Имена: `thumb_leaves.jpg`, `thumb_rain.jpg`, `thumb_snow.jpg`, `thumb_lightning.jpg`,
  `thumb_wind.jpg`, `thumb_storm.jpg`, `thumb_sunset_snow.jpg`, `thumb_night.jpg`

По умолчанию задействованы XML-градиенты `thumb_*.xml`, так что проект собирается без
растровых файлов. Чтобы включить реальные превью, добавьте изображения с теми же именами —
они автоматически перекроют XML.

## Иконки (опционально)

Если нужны пиктограммы для подписей, используйте векторные XML-файлы в `app/src/main/res/drawable/`:
`ic_leaf.xml`, `ic_rain.xml`, `ic_snow.xml`, `ic_lightning.xml`, `ic_wind.xml`, `ic_storm.xml`,
`ic_sunset.xml`, `ic_night.xml`.

# Превью для нижней карусели эффектов

Карточки в `EffectCarousel` используют список из `R.array.abys_effect_thumbs`. В репозитории лежат
только XML-градиенты (`thumb_*.xml`). Чтобы подменить их на реальные картинки, положите файлы
в `drawable-nodpi` с теми же именами.

> Совет: изображения внутри `drawable-nodpi` не масштабируются по плотности экрана, поэтому они
> будут отображаться в исходном качестве.

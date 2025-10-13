# Ассеты для погодных тем

Карусель использует растровые файлы из `app/src/main/res/drawable-nodpi`. Фон сцены фиксированный,
поэтому для каждой темы нужно подготовить только квадратные превью.

## Превью для плиток карусели

* Путь: `app/src/main/res/drawable-nodpi/`
* Формат: квадрат 512×512, JPEG/PNG без прозрачности
* Имена: `thumb_leaves.jpg`, `thumb_rain.jpg`, `thumb_snow.jpg`, `thumb_lightning.jpg`,
  `thumb_wind.jpg`, `thumb_storm.jpg`, `thumb_sunset_snow.jpg`, `thumb_night.jpg`

Указанные ресурсы подключаются в `ThemeEffect.kt` через поле `thumbRes`.

## Иконки (опционально)

Если нужны пиктограммы для подписей, используйте векторные XML-файлы в `app/src/main/res/drawable/`:
`ic_leaf.xml`, `ic_rain.xml`, `ic_snow.xml`, `ic_lightning.xml`, `ic_wind.xml`, `ic_storm.xml`,
`ic_sunset.xml`, `ic_night.xml`.

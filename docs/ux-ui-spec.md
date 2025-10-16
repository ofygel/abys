# UX/UI Specification

## 1. Overview
This document formalises the product design for the three primary surfaces of the Abys application:

1. **Home – Prayer timetable**
2. **Glass sheet – Hadith & city details**
3. **City picker – Vertical carousel**

The specification consolidates the stakeholder notes, removes small inconsistencies, and adds missing behavioural guidance so designers and engineers share the same source of truth. Pixel values reference the 636×1131 px artboard unless stated otherwise. All dimensions scale with:

```
sx = viewportWidth / 636
sy = viewportHeight / 1131
s  = (sx + sy) / 2              // use for radii & blur
```

All typography uses Inter (fallback Roboto/system). For iOS substitute SF Pro with matching weights. Colours default to sRGB.

---

## 2. Design tokens

| Token | Value | Notes |
| --- | --- | --- |
| `color.textPrimary` | `#FFFFFF` | All foreground text & icons |
| `color.textShadow` | `rgba(0,0,0,0.35)` | Apply to white text for contrast |
| `color.overlayDark` | `rgba(0,0,0,0.36)` | Header & home card glass; split difference between 0.35/0.38 specs |
| `color.overlayLight` | `rgba(255,255,255,0.20)` | Light glass sheet. Slightly lighter than 0.26 to offset blur brightening |
| `color.timelineTick` | `rgba(255,255,255,1)` | Timeline dividers |
| `color.timelineTickGhost` | `rgba(255,255,255,0.5)` | Optional guidelines |
| `color.cityChipStroke` | `rgba(255,255,255,0.36)` | Outline-only chip |
| `color.cityTick` | `rgba(0,0,0,0.85)` | Picker brackets |
| `radius.header` | `35·s` | Header pill |
| `radius.card` | `30·s` | Main prayer card |
| `radius.thumb` | `22·s` | Effect thumbnails |
| `radius.sheet` | `28·s` | Glass sheet |
| `radius.contentFrame` | `(64·sx, 56·sy)` | Elliptical frame inside sheet |
| `blur.glass` | `8·s` | Optional glass blur |
| `shadow.card` | `0 6·s 24·s rgba(0,0,0,0.35)` | For floating glass components |

Typography tokens (base size shown at 636×1131):

| Token | Weight | Size | Line height | Decoration |
| --- | --- | --- | --- | --- |
| `type.city` | 600 Italic | 57 px | 1.05 | Underline 2 px |
| `type.timeNow` | 700 | 59 px | 1.0 | – |
| `type.prayer` | 700 | 44 px | 1.12 | – |
| `type.prayerSub` | 700 | 41 px | 1.12 | – |
| `type.timeline` | 700 | 38 px | 1.18 | – |
| `type.cityPicker0` | 800 | 42 px | 1.10 | – |
| `type.cityPicker1` | 700 | 32 px | 1.08 | – |
| `type.cityPicker2` | 700 | 26 px | 1.05 | – |
| `type.cityPickerFar` | 700 | clamp(22 px, 3.4vw, 24 px) | 1.00 | Used off-screen |

Scale typography with the same factor as other dimensions (`size·sy`). Minimum legible size is 18 px on small phones.

---

## 3. Screen 1 — Home / Prayer timetable

### 3.1 Layout

| Element | X | Y | Width | Height | Notes |
| --- | --- | --- | --- | --- | --- |
| Header pill | 67·sx | 79·sy | 533·sx | 102·sy | Background `color.overlayDark`, radius `radius.header`, optional blur |
| City label | 87·sx | baseline 99·sy | – | – | `type.city`, underline thickness 3·sy, offset 2·sy |
| Current time | right inset 20·sx | baseline 99·sy | – | – | `type.timeNow`, right-aligned |
| Prayer card | 64·sx | 226·sy | 508·sx | 611·sy | Background `color.overlayDark`, radius `radius.card`, padding: 44 L/R, 45 top, 40 bottom |
| Labels column | 108·sx | – | – | – | All left text anchored here |
| Values column | 528·sx | – | – | – | Right-aligned times |
| Row step | – | – | – | 73·sy | Measured between baselines |
| Bottom timeline | centre y ≈ 741·sy | – | – | – | See §3.3 |
| Effect carousel | tile centres y = 905·sy | – | – | – | See §3.4 |

### 3.2 Prayer rows

1. **Фаджр** – 04:50
2. **Восход** – 06:28
3. **Зухр** – 12:10
4. **Аср** (parent row, empty right column)
   - *стандарт* — indicator bar at x = 388·sx (width 64·sx, height 4·sy, radius 2·s), time 15:05
   - *Ханафи* — same geometry, time 15:39
5. **Магриб** – 17:28
6. **Иша** – 18:40

Use token `type.prayer` for row labels and right column; `type.prayerSub` for Asr sub-rows. For localisation ensure strings can expand 15% without clipping.

### 3.3 Timeline of the night

- Baseline y = 741·sy.
- Groups at x = 145·sx, 303·sx, 456·sx.
- Each group: three 3·sx thick ticks, centre height 57·sy, side ticks 49·sy with round caps.
- Labels below at y = 753·sy using `type.timeline`, centred to group x.
- Default labels: 21:12, 00:59, 04:50. When night fractions are recalculated use dynamic labels following the thirds computation noted in §6.2.

### 3.4 Effect carousel (bottom)

- Tile size 121×153 px (scale with `sx`, `sy`), radius `radius.thumb`.
- Centres at x = 0.18·W, 0.50·W, 0.82·W.
- Active tile scale 1.0 / opacity 1.0; neighbours scale 0.85 / opacity 0.7; others scale 0.75 / opacity 0.5.
- Gesture: horizontal inertial scroll with snap to nearest centre. Tap triggers snap if not centred; centred tap fires `onEffectSelected(effectId)`.
- Assets stored in `res/drawable-nodpi/` using names `thumb_<id>.jpg`. IDs: leaves, lightning, night, rain, snow, storm, sunset_snow, wind.

### 3.5 Interactions

- **Greeting transition:** when intro finishes fade out (600 ms easeOutQuad), staggered fade/slide/scale in header (280 ms), prayer card (320 ms), carousel (260 ms).
- **City pill tap:** triggers `showCitySheet()`; prayer card and carousel fade out 220 ms, sheet fades in 220 ms.
- **Prayer card double-tap:** toggles explode/assemble animation (see §5.2).

---

## 4. Screen 2 — Glass sheet (Hadith / City overview)

### 4.1 Panel

- Bounds: x = 69·sx, y = 54·sy, width = 504·sx, height = 990·sy.
- Radius = `radius.sheet`.
- Background `color.overlayLight` with `blur.glass` applied beneath when API ≥ 31; fall back to pure alpha on lower APIs.
- Optional shadow `0 10·s 40·s rgba(0,0,0,0.25)`.

### 4.2 City chip

- Inset: left/right 56·sx, top 72·sy, height 64·sy.
- Outline only: 3·sx stroke `color.cityChipStroke`, radius 24·s, transparent fill.
- Text: centred `type.city` but clamp font-size to `clamp(24·sy, 3.8vw, 36·sy)` for long names. Ellipsis if wider than 80% panel width after scaling.

### 4.3 Content frame (Hadith)

- Position: left/right inset 72·sx, top offset 200·sy below chip bottom, bottom inset 120·sy.
- Border: 5·sx stroke `rgba(0,0,0,0.85)`, radius elliptical `64·sx / 56·sy`.
- Padding: 32·sy top/bottom, 36·sx left/right.
- Text: Inter Bold with adaptive size `clamp(18·sy, 2.6vw, 26·sy)`; line-height 1.42; text-shadow `color.textShadow`.
- Behaviour: first shrink font (down to 18 px base) before enabling vertical scroll. When scrollable show subtle translucent scrollbar.

### 4.4 State machine

1. `showCitySheet()` – from home.
2. Chip tap toggles picker state: `showCityPicker()` / `hideCityPicker()`.
3. When picker hidden, hadith frame is visible; when picker visible, hadith frame fades out in 180 ms before picker fades in 220 ms.
4. Selecting a city triggers `onCitySelected(city)`; hadith remains hidden until new times are loaded, then sheet closes and home returns via crossfade 220 ms.

### 4.5 Accessibility

- Ensure chip, hadith frame and any buttons have 48 dp hit targets.
- Provide TalkBack descriptions: chip announces current city and “double tap to change city”; hadith frame announces scrollable state.

---

## 5. Screen 3 — City picker (vertical carousel)

### 5.1 Panel & chrome

- Uses same panel geometry as §4.1.
- Two horizontal ticks define the active zone at y = 549·sy. Each tick is 110·sx long, 6·sy thick with round caps; colour `color.cityTick`. Left tick span x = 129·sx → 239·sx; right tick x = 403·sx → 513·sx.
- Apply vertical fade mask of 160·sy on top and bottom (transparent to 90% white alpha) to soften list edges.

### 5.2 Item metrics

- Step between item centres = 92·sy.
- Visible stack = 5 items; off-screen items continue above/below for smooth flings.
- Font scaling and opacity follow Gaussian curves:
  - `size(d) = clamp(22·sy, 42·sy * (0.60 + 0.40 · e^{-(d/1.2)^2}), 42·sy)`
  - `opacity(d) = clamp(0.25, e^{-(d/1.15)^2}, 1.0)`
  where `d = |index − activeIndex|`.
- Weights: active 800, others 700.
- All items share text-shadow `color.textShadow` for legibility.

### 5.3 Behaviour

- Centre snap at y = 549·sy.
- Tap on non-active item scrolls it into centre (220–280 ms easeOutQuad).
- Vertical fling uses physics deceleration ≈ 2800 px/s²; always ends on nearest item.
- Hit zone min 48·sy height around each centre. Provide haptic feedback when snap completes.
- TalkBack roving index: only the active item is focusable. Announce as “City: <name>, double tap to select”.

### 5.4 City dataset

```
Almaty, Astana, Shymkent, Karaganda, Pavlodar,
Aktobe, Taraz, Oskemen, Semey, Kostanay,
Kyzylorda, Atyrau, Uralsk, Taldykorgan, Petropavl,
Ekibastuz, Temirtau, Aktau, Kokshetau
```

Ensure localisation handles Latin, Cyrillic and Kazakh alphabets uniformly; disallow line breaks.

---

## 6. Behavioural rules

### 6.1 Data refresh

- Persist selected city (string key) in `SettingsStore`.
- On `onCitySelected`, update stored city, recalculate times, then animate numbers via staggered 150 ms crossfade per row (50 ms delay between rows) before closing sheet.

### 6.2 Night thirds calculation

- `nightDuration = nextFajr - maghrib`.
- `thirdLength = nightDuration / 3`.
- `T1 = maghrib + thirdLength`, `T2 = T1 + thirdLength`, `T3 = nextFajr`.
- Timeline labels correspond to maghrib, T1, T2, T3 (with T3 re-using Fajr time). Update the text fields accordingly.

### 6.3 Double-tap explode

- Split prayer card visual into 4×6 grid (24 fragments).
- Hide animation: each fragment translates 120–220·s px outward with rotation ±25°–60°, 300–450 ms easeOutCubic + random 0–60 ms delay. Card simultaneously fades to 0 in 220 ms.
- Show animation: reverse path with easeOutBack 320–480 ms.
- Disable interactions with tiles while exploded.

---

## 7. Implementation checklist

1. Background imagery always `object-fit: cover`, no extra dim layer (rely on glass overlays).
2. Header pill responds to tap (visual feedback via subtle opacity dip to 0.88 for 120 ms).
3. Prayer card uses glass blur where supported; otherwise static alpha.
4. Asr displays both standard and Hanafi times with indicators aligned at x = 388·sx.
5. Night timeline recalculates thirds automatically per §6.2.
6. Effect carousel uses JPG thumbnails named `thumb_<id>.jpg` placed in `res/drawable-nodpi/`.
7. City sheet uses chip → hadith/picker toggle with transitions described in §4.4.
8. City picker implements Gaussian scaling, fade mask, snap physics and dataset of 19 cities.
9. Double-tap explode/assemble works symmetrically and restores interactions afterwards.
10. All tap targets respect 48 dp minimum; add TalkBack labels for header, chip, picker and carousel tiles.

---

## 8. Assets & deliverables

- **Design source:** maintain a Figma frame sized 636×1131 px with auto-layout components matching this spec.
- **Raster assets:** supply JPG thumbnails listed in §3.4 and high-resolution background images (minimum 1242×2208) to the art team.
- **Font bundle:** ensure Inter Regular/Bold/Italic are embedded or reference system fallbacks per platform.

---

## 9. Revision history

| Version | Date | Author | Notes |
| --- | --- | --- | --- |
| 1.0 | 2024-04-05 | Design Systems | Initial consolidated specification |


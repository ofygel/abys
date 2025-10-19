@file:Suppress("MagicNumber")

package com.example.abys.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abys.R
import com.example.abys.logic.NightIntervals
import com.example.abys.ui.theme.AbysFonts
import com.example.abys.ui.theme.Dimens
import com.example.abys.ui.theme.Tokens
import com.example.abys.ui.util.backdropBlur

internal object GlassDefaults {
    val top: Color
        @Composable get() = Tokens.Colors.overlayTop.copy(alpha = 0.9f)
    val bottom: Color
        @Composable get() = Tokens.Colors.overlayCard.copy(alpha = 0.86f)
    val stroke: Color
        @Composable get() = Color.White.copy(alpha = 0.32f)
    val glow: Color
        @Composable get() = Color.White.copy(alpha = 0.18f)
    val blur
        @Composable get() = (10f * Dimens.s()).dp
    val elevation
        @Composable get() = (24f * Dimens.s()).dp
    val bgScrim = Color.Black.copy(alpha = 0.25f)
}

internal object TypeTone {
    val primary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.96f)
    val secondary: Color
        @Composable get() = Tokens.Colors.text.copy(alpha = 0.88f)
    val divider: Color
        @Composable get() = Color.White.copy(alpha = 0.16f)
}

private fun scaledSp(basePx: Int, scale: Float) = (basePx * scale).sp

internal object TypeScale {
    val city = scaledSp(Tokens.TypographyPx.city, 0.68f)
    val timeNow = scaledSp(Tokens.TypographyPx.timeNow, 0.68f)
    val label = scaledSp(Tokens.TypographyPx.label, 0.56f)
    val subLabel = scaledSp(Tokens.TypographyPx.subLabel, 0.54f)
    val timeline = scaledSp(Tokens.TypographyPx.timeline, 0.54f)
    val hadith = scaledSp(Tokens.TypographyPx.timeline, 0.6f)
}

private const val TABULAR_FEATURE = "'tnum'"

private val TabularFeatureStyle = TextStyle(fontFeatureSettings = TABULAR_FEATURE)

@Composable
internal fun TabularText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = overflow,
        style = LocalTextStyle.current.merge(TabularFeatureStyle)
    )
}

@Composable
internal fun ThinDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = TypeTone.divider,
        thickness = 1.dp
    )
}

@Composable
internal fun PrayerDashboard(
    city: String,
    now: String,
    prayerTimes: Map<String, String>,
    thirds: NightIntervals,
    hadith: String,
    onCityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sy = Dimens.sy()
    val sx = Dimens.sx()
    val shape = RoundedCornerShape(Tokens.Radii.card())
    val cardHorizontalPad = Dimens.scaledX(R.dimen.abys_card_pad_h)
    val cardTopPad = Dimens.scaledY(R.dimen.abys_card_pad_top)
    val cardBottomPad = Dimens.scaledY(R.dimen.abys_card_pad_bottom)
    val contentSpacing = (28f * sy).dp
    val columnsSpacing = (40f * sx).dp

    Box(
        modifier
            .fillMaxWidth()
            .shadow(elevation = GlassDefaults.elevation, shape = shape, clip = false)
            .clip(shape)
            .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .backdropBlur(GlassDefaults.blur)
                .background(Brush.verticalGradient(listOf(GlassDefaults.top, GlassDefaults.bottom)))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(0f to GlassDefaults.stroke, 1f to GlassDefaults.glow),
                    shape = shape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = cardHorizontalPad,
                    end = cardHorizontalPad,
                    top = cardTopPad,
                    bottom = cardBottomPad
                ),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            CityHeader(
                city = city,
                now = now,
                onTap = onCityClick,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(columnsSpacing)
            ) {
                PrayerSchedule(
                    times = prayerTimes,
                    thirds = thirds,
                    modifier = Modifier.weight(1f)
                )

                HadithPanel(
                    text = hadith,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CityHeader(city: String, now: String, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val sy = Dimens.sy()
    val sx = Dimens.sx()
    val indicatorWidth = (64f * sx).dp
    val indicatorHeight = (2f * sy).dp

    Column(
        modifier
            .semantics {
                contentDescription = "Открыть выбор города"
                role = Role.Button
            }
            .clickable(onClick = onTap)
            .padding(horizontal = (12f * sx).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy((12f * sy).dp)
    ) {
        Text(
            text = city.ifBlank { "—" },
            fontSize = TypeScale.city,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = TypeTone.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        TabularText(
            text = now.ifBlank { "--:--" },
            fontSize = TypeScale.timeNow,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.secondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(indicatorHeight)
                .background(TypeTone.divider)
        )
    }
}

@Composable
private fun PrayerSchedule(times: Map<String, String>, thirds: NightIntervals, modifier: Modifier = Modifier) {
    val sy = Dimens.sy()
    val rowSpacing = (12f * sy).dp
    val sectionSpacing = (24f * sy).dp
    val nightHeadingSpacing = (10f * sy).dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
            val schedule = listOf(
                "Фаджр" to (times["Fajr"] ?: "--:--"),
                "Восход" to (times["Sunrise"] ?: "--:--"),
                "Зухр" to (times["Dhuhr"] ?: "--:--"),
                "Аср (Стандарт)" to (times["AsrStd"] ?: "--:--"),
                "Аср (Ханафитский)" to (times["AsrHana"] ?: "--:--"),
                "Магриб" to (times["Maghrib"] ?: "--:--"),
                "Иша" to (times["Isha"] ?: "--:--")
            )

            schedule.forEachIndexed { index, (label, value) ->
                PrayerRow(label, value)
                if (index != schedule.lastIndex) {
                    Spacer(Modifier.height(rowSpacing))
                    ThinDivider()
                    Spacer(Modifier.height(rowSpacing))
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(nightHeadingSpacing)) {
            NightSectionHeading()
            NightThirdsRow(thirds)
        }
    }
}

@Composable
private fun PrayerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = TypeScale.label,
            fontWeight = FontWeight.Medium,
            color = TypeTone.secondary,
            lineHeight = TypeScale.label,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        TabularText(
            text = value,
            fontSize = TypeScale.label,
            fontWeight = FontWeight.SemiBold,
            color = TypeTone.primary,
            textAlign = TextAlign.Right,
            lineHeight = TypeScale.label,
            modifier = Modifier.widthIn(min = 0.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun NightSectionHeading() {
    Text(
        text = "Ночь (3 части)",
        fontSize = TypeScale.label,
        fontWeight = FontWeight.SemiBold,
        color = TypeTone.primary
    )
}

@Composable
private fun NightThirdsRow(thirds: NightIntervals) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val spacing = (10f * sx).dp
    val cardHeight = (72f * sy).dp
    val shape = RoundedCornerShape((16f * sy).dp)
    val borderColor = Color.White.copy(alpha = 0.26f)
    val background = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.22f),
        1f to Color.White.copy(alpha = 0.08f)
    )
    val romans = listOf("I", "II", "III")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        thirds.asList().forEachIndexed { index, (start, end) ->
            NightThirdCard(
                title = romans.getOrElse(index) { "" },
                start = start.ifBlank { "--:--" },
                end = end.ifBlank { "--:--" },
                modifier = Modifier.weight(1f),
                height = cardHeight,
                shape = shape,
                borderColor = borderColor,
                background = background
            )
        }
    }
}

@Composable
private fun NightThirdCard(
    title: String,
    start: String,
    end: String,
    modifier: Modifier,
    height: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    borderColor: Color,
    background: Brush
) {
    val sx = Dimens.sx()
    val sy = Dimens.sy()
    val spacing = (6f * sy).dp
    val timeSize = TypeScale.timeline

    Box(
        modifier
            .height(height)
            .clip(shape)
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (10f * sx).dp, vertical = (12f * sy).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = TypeScale.subLabel,
                fontWeight = FontWeight.Bold,
                color = TypeTone.secondary
            )
            Spacer(Modifier.height(spacing))
            TabularText(
                text = start,
                fontSize = timeSize,
                fontWeight = FontWeight.SemiBold,
                color = TypeTone.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            ThinDivider(Modifier.padding(vertical = (4f * sy).dp))
            TabularText(
                text = end,
                fontSize = timeSize,
                fontWeight = FontWeight.SemiBold,
                color = TypeTone.primary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HadithPanel(text: String, modifier: Modifier = Modifier) {
    val sy = Dimens.sy()
    val sx = Dimens.sx()
    val shape = RoundedCornerShape((22f * sy).dp)

    Box(
        modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.18f), shape = shape)
            .padding(horizontal = (18f * sx).dp, vertical = (22f * sy).dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = text.ifBlank { "—" },
            fontSize = TypeScale.hadith,
            fontWeight = FontWeight.Medium,
            color = TypeTone.primary,
            lineHeight = TypeScale.hadith * 1.3f,
            textAlign = TextAlign.Center,
            fontFamily = AbysFonts.inter,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

